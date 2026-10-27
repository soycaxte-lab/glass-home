package local.glasshome;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/** Visible-only fixture panel backed by explicitly offered iCalendar subscriptions. */
final class FootballRow extends LinearLayout {
 private final MainActivity home;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final TextView[] tabs=new TextView[2];
 private final ArrayList<View> cards=new ArrayList<>();
 private final LinearLayout list;
 private final TextView source;
 private TextView teamsButton;
 private final FootballTeams catalog;
 private List<FootballTeams.Team> teams;
 private AlertDialog teamDialog;
 private final ArrayList<FootballFixtures.Match> matches=new ArrayList<>();
 private boolean active=false; private int generation=0,selected=0,available=0,fresh=0;
 private Future<?> pending; private long lastMemoryCheck; private String renderedState="";
 private static final long TTL=6*60*60*1000L,RETRY=15*60*1000L;
 private final Runnable midnight=()->{render();startLoad();scheduleMidnight();};

 FootballRow(MainActivity h){
  super(h);home=h;
  FootballTeams loaded;try{loaded=new FootballTeams(new InputStreamReader(h.getAssets().open("football-teams.tsv"),StandardCharsets.UTF_8));}catch(IOException ex){try{loaded=new FootballTeams(new StringReader(""));}catch(IOException impossible){throw new IllegalStateException(impossible);}}
  catalog=loaded;teams=catalog.restore(h.getPreferences(0).getString(FootballTeams.PREFERENCE,null));
  setOrientation(VERTICAL);setPadding(h.dp(12),h.dp(8),h.dp(12),h.dp(6));setBackground(new android.graphics.drawable.LayerDrawable(new android.graphics.drawable.Drawable[]{h.shape(0x85303769,20),h.surface(20,false,0x243c4863)}));setClipChildren(false);
  LinearLayout bar=new LinearLayout(h);bar.setGravity(Gravity.CENTER_VERTICAL);addView(bar,new LayoutParams(-1,h.dp(24)));
  TextView heading=h.text("Fútbol",13,0xffedf3ff);bar.addView(heading,new LayoutParams(0,-2,1));
  for(int i=0;i<2;i++){
   final int index=i;TextView tab=h.text(i==0?"Hoy":"Mañana",11,0xffd0d9ef);tab.setGravity(Gravity.CENTER);tab.setFocusable(true);tab.setClickable(true);tab.setPadding(h.dp(9),0,h.dp(9),0);tabs[i]=tab;bar.addView(tab,new LayoutParams(-2,h.dp(24)));
   tab.setOnClickListener(v->{selected=index;render();});
   tab.setOnFocusChangeListener((v,f)->{if(f){selected=index;render();h.glass.setVisibility(INVISIBLE);h.scroll.smoothScrollTo(0,0);}else styleTabs();});
   tab.setOnKeyListener((v,k,e)->{if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==19)return true;if(k==20){if(!cards.isEmpty())cards.get(0).requestFocus();else dock();return true;}if(k==21){if(index>0)tabs[index-1].requestFocus();return true;}if(k==22){if(index==0)tabs[1].requestFocus();else teamsButton.requestFocus();return true;}return false;});
  }
  teamsButton=h.text("⚙",13,0xffe4eeff);teamsButton.setGravity(Gravity.CENTER);teamsButton.setFocusable(true);teamsButton.setClickable(true);teamsButton.setContentDescription("Cambiar equipos de fútbol");bar.addView(teamsButton,new LayoutParams(h.dp(25),h.dp(24)));
  teamsButton.setOnClickListener(v->showTeams());teamsButton.setOnFocusChangeListener((v,f)->{teamsButton.setBackground(h.surface(12,f,0x103c4863));if(f){h.glass.setVisibility(INVISIBLE);h.scroll.smoothScrollTo(0,0);}});
  teamsButton.setOnKeyListener((v,k,e)->{if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==21){tabs[1].requestFocus();return true;}if(k==22){h.top.getChildAt(0).requestFocus();return true;}if(k==20){if(cards.isEmpty())dock();else cards.get(0).requestFocus();return true;}return k==19;});
  ScrollView scroll=new ScrollView(h);scroll.setVerticalScrollBarEnabled(false);scroll.setFocusable(false);scroll.setClipToPadding(false);LayoutParams sp=new LayoutParams(-1,0,1);sp.topMargin=h.dp(3);addView(scroll,sp);
  list=new LinearLayout(h);list.setOrientation(VERTICAL);scroll.addView(list,new ScrollView.LayoutParams(-1,-2));
  source=h.text("Cargando calendarios…",8,0xffaebed9);source.setPadding(0,h.dp(3),0,0);addView(source,new LayoutParams(-1,h.dp(15)));
  render();
 }
 boolean focusTab(){tabs[selected].requestFocus();return true;}
 private void left(){tabs[selected].requestFocus();}
 private void dock(){if(home.watch.focusCard(home.lastDock))return;if(!home.dockTiles.isEmpty())home.dockTiles.get(Math.min(home.lastDock,home.dockTiles.size()-1)).requestFocus();}
 private void styleTabs(){for(int i=0;i<2;i++){boolean f=tabs[i].hasFocus();tabs[i].setBackground(home.surface(12,f,i==selected?0x705574a1:0x103c4863));tabs[i].setTextColor(i==selected?0xffffffff:0xffaebed9);}}
 void start(){active=true;render();if(lastMemoryCheck==0||SystemClock.elapsedRealtime()-lastMemoryCheck>=60000)startLoad();scheduleMidnight();}
 void stop(){active=false;generation++;handler.removeCallbacks(midnight);if(pending!=null)pending.cancel(true);}
 void destroy(){stop();if(teamDialog!=null)teamDialog.dismiss();worker.shutdownNow();}
 private void scheduleMidnight(){handler.removeCallbacks(midnight);long wait=Duration.between(Instant.now(),LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()).toMillis();if(active)handler.postDelayed(midnight,Math.max(1000,wait+1000));}
 private File file(String id){return new File(home.getFilesDir(),"football-"+id+".ics");}
 private void startLoad(){
  if(!active)return;final int gen=++generation;final List<FootballTeams.Team> subscriptions=new ArrayList<>(teams);if(pending!=null)pending.cancel(true);
  pending=worker.submit(()->{
   ArrayList<FootballFixtures.Match> all=new ArrayList<>();int loaded=0,current=0;
   // First show disk cache without waiting for the network.
   for(int i=0;i<subscriptions.size();i++)try{File f=file(subscriptions.get(i).id);if(f.isFile()){all.addAll(FootballFixtures.parse(readFile(f),i));loaded++;if(System.currentTimeMillis()-f.lastModified()<TTL)current++;}}catch(Exception ignored){}
   publish(all,loaded,current,gen);
   for(int i=0;i<subscriptions.size();i++){
    if(Thread.currentThread().isInterrupted())return;File f=file(subscriptions.get(i).id);long now=System.currentTimeMillis();
    if(f.isFile()&&now-f.lastModified()<TTL)continue;
    String pref="football-attempt-id-"+subscriptions.get(i).id;long attempt=home.getPreferences(0).getLong(pref,0);if(now-attempt<RETRY)continue;
    home.getPreferences(0).edit().putLong(pref,now).apply();
    try{String body=download("https://ics.fixtur.es/v2/"+subscriptions.get(i).id+".ics");FootballFixtures.parse(body,i);
     android.util.AtomicFile atomic=new android.util.AtomicFile(f);FileOutputStream out=null;
     try{out=atomic.startWrite();out.write(body.getBytes(StandardCharsets.UTF_8));atomic.finishWrite(out);}catch(IOException ex){if(out!=null)atomic.failWrite(out);throw ex;}
    }catch(Exception ex){android.util.Log.w("GlassFootball","Calendar unavailable: "+subscriptions.get(i).id);}
   }
   if(Thread.currentThread().isInterrupted())return;all.clear();loaded=0;current=0;
   for(int i=0;i<subscriptions.size();i++)try{File f=file(subscriptions.get(i).id);if(f.isFile()){all.addAll(FootballFixtures.parse(readFile(f),i));loaded++;if(System.currentTimeMillis()-f.lastModified()<TTL)current++;}}catch(Exception ignored){}
   publish(all,loaded,current,gen);
  });
 }
 private void publish(ArrayList<FootballFixtures.Match> all,int loaded,int current,int gen){ArrayList<FootballFixtures.Match> snapshot=new ArrayList<>(all);handler.post(()->{if(!active||gen!=generation)return;matches.clear();matches.addAll(snapshot);available=loaded;fresh=current;lastMemoryCheck=SystemClock.elapsedRealtime();render();});}
 private static String readFile(File f)throws IOException{try(InputStream in=new FileInputStream(f)){return read(in);}}
 private static String read(InputStream in)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int count;while((count=in.read(buffer))!=-1){if(out.size()+count>2*1024*1024)throw new IOException("Calendar too large");out.write(buffer,0,count);}return out.toString(StandardCharsets.UTF_8.name());}
 private static String download(String link)throws IOException{
  HttpURLConnection c=(HttpURLConnection)new URL(link).openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setInstanceFollowRedirects(false);c.setRequestProperty("Accept","text/calendar");c.setRequestProperty("User-Agent","GlassHome/1.6 (personal calendar subscription)");
  try{if(c.getResponseCode()!=200)throw new IOException("Calendar HTTP "+c.getResponseCode());try(InputStream in=c.getInputStream()){return read(in);}}finally{c.disconnect();}
 }
 private void render(){
  styleTabs();ZoneId zone=ZoneId.systemDefault();LocalDate day=LocalDate.now(zone).plusDays(selected);List<FootballFixtures.Match> today=FootballFixtures.forDay(matches,day,zone);
  StringBuilder state=new StringBuilder(day.toString()).append(zone.getId()).append(':').append(available).append(':').append(fresh).append(':').append(catalog.serialize(teams));
  for(FootballFixtures.Match m:today)state.append('|').append(m.team).append('|').append(m.title).append('|').append(m.kickoff).append('|').append(m.competition);
  if(renderedState.contentEquals(state))return;renderedState=state.toString();
  View focused=home.getCurrentFocus();int focusIndex=cards.indexOf(focused);list.removeAllViews();cards.clear();
  if(today.isEmpty()){
   TextView empty=home.text(available<teams.size()?"Calendarios pendientes de actualizar":"Sin partidos "+(selected==0?"hoy":"mañana"),10,0xffd3ddef);empty.setPadding(0,home.dp(3),0,home.dp(3));list.addView(empty);
   TextView favorites=home.text(favoriteNames(),9,0xffaebed9);favorites.setMaxLines(2);favorites.setEllipsize(android.text.TextUtils.TruncateAt.END);list.addView(favorites);
  }
  for(int i=0;i<today.size();i++){
   FootballFixtures.Match m=today.get(i);final int index=i;LinearLayout card=new LinearLayout(home);card.setGravity(Gravity.CENTER_VERTICAL);card.setPadding(home.dp(5),home.dp(3),home.dp(5),home.dp(3));card.setFocusable(true);card.setClickable(true);card.setBackground(home.surface(12,false,0x183c4863));LayoutParams cp=new LayoutParams(-1,home.dp(44));cp.bottomMargin=home.dp(3);list.addView(card,cp);cards.add(card);
   int badgeIndex=m.team>=0&&m.team<teams.size()?teams.get(m.team).badge():-1;int res=badgeIndex<0?0:home.getResources().getIdentifier("football_"+badgeIndex,"drawable",home.getPackageName());if(res!=0){ImageView badge=new ImageView(home);badge.setImageResource(res);badge.setScaleType(ImageView.ScaleType.FIT_CENTER);LayoutParams bp=new LayoutParams(home.dp(25),home.dp(25));bp.rightMargin=home.dp(8);card.addView(badge,bp);}else if(m.team>=0&&m.team<teams.size()){TextView initials=home.text(initials(teams.get(m.team).name),10,0xffe4eeff);initials.setGravity(Gravity.CENTER);initials.setBackground(home.shape(0xff405781,8));LayoutParams bp=new LayoutParams(home.dp(25),home.dp(25));bp.rightMargin=home.dp(8);card.addView(initials,bp);}
   LinearLayout labels=new LinearLayout(home);labels.setOrientation(VERTICAL);card.addView(labels,new LayoutParams(0,-2,1));TextView title=home.text(m.title,10,0xfff0f4ff);title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(title);
   String hour=m.kickoff==null?"Horario por confirmar":DateTimeFormatter.ofPattern("HH:mm").withZone(zone).format(m.kickoff);TextView time=home.text(hour+(m.competition.isEmpty()?"":" · "+m.competition),9,0xffb0c5ea);time.setSingleLine(true);time.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(time);
   card.setContentDescription(m.title+", "+hour);
   card.setOnFocusChangeListener((v,f)->{v.setBackground(home.surface(12,f,0x183c4863));if(f){home.glass.setVisibility(INVISIBLE);home.scroll.smoothScrollTo(0,0);}});
   card.setOnClickListener(v->new AlertDialog.Builder(home).setTitle(m.title).setMessage((selected==0?"Hoy":"Mañana")+" · "+hour+"\n"+zone.getId()+(m.competition.isEmpty()?"":"\n"+m.competition)+"\n\nCalendario: Fixtur.es\nLos horarios pueden cambiar. No incluye transmisión ni marcador en directo.").setPositiveButton("Cerrar",null).show());
   card.setOnKeyListener((v,k,e)->{if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==19){if(index==0)tabs[selected].requestFocus();else cards.get(index-1).requestFocus();return true;}if(k==20){if(index+1<cards.size())cards.get(index+1).requestFocus();else dock();return true;}if(k==21){left();return true;}if(k==22){home.top.getChildAt(0).requestFocus();return true;}return false;});
  }
  source.setText("Fixtur.es · "+(available<teams.size()?available+"/"+teams.size()+" calendarios":fresh<teams.size()?"Datos guardados; actualización pendiente":"Hora local · Sin directo"));
  if(focusIndex>=0){if(cards.isEmpty())tabs[selected].requestFocus();else cards.get(Math.min(focusIndex,cards.size()-1)).requestFocus();}
 }
 private String favoriteNames(){ArrayList<String> names=new ArrayList<>();for(FootballTeams.Team t:teams)names.add(t.name);return String.join(" · ",names);}
 private static String initials(String label){StringBuilder result=new StringBuilder();for(String part:label.split("\\s+")){if(part.isEmpty()||part.equalsIgnoreCase("de")||part.equalsIgnoreCase("selección"))continue;result.append(part.charAt(0));if(result.length()==2)break;}return result.toString().toUpperCase(Locale.ROOT);}
 void saveTeams(Collection<FootballTeams.Team> selection){
  String encoded=catalog.serialize(selection);
  if(encoded.equals(catalog.serialize(teams)))return;
  home.getPreferences(0).edit().putString(FootballTeams.PREFERENCE,encoded).apply();
  teams=catalog.restore(encoded);generation++;if(pending!=null)pending.cancel(true);matches.clear();available=0;fresh=0;lastMemoryCheck=0;renderedState="";render();if(active)startLoad();
 }
 void showTeams(){
  if(teamDialog!=null&&teamDialog.isShowing())return;
  final LinkedHashMap<String,FootballTeams.Team> chosen=new LinkedHashMap<>();for(FootballTeams.Team t:teams)chosen.put(t.id,t);
  LinearLayout body=new LinearLayout(home);body.setOrientation(VERTICAL);body.setPadding(home.dp(20),home.dp(4),home.dp(20),0);
  EditText search=new EditText(home);search.setSingleLine(true);search.setTextSize(15);search.setHint("Buscar equipo, selección o país");search.setContentDescription("Buscar equipos de fútbol");body.addView(search,new LayoutParams(-1,home.dp(42)));
  TextView count=home.text("",12,0xffcbdcf5);count.setPadding(0,home.dp(6),0,home.dp(6));body.addView(count,new LayoutParams(-1,-2));
  ListView options=new ListView(home);options.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);body.addView(options,new LayoutParams(-1,home.dp(210)));
  ArrayList<FootballTeams.Team> visible=new ArrayList<>();ArrayAdapter<FootballTeams.Team> adapter=new ArrayAdapter<>(home,android.R.layout.simple_list_item_multiple_choice,visible);options.setAdapter(adapter);
  Runnable refresh=()->{visible.clear();visible.addAll(catalog.search(search.getText().toString()));adapter.notifyDataSetChanged();options.clearChoices();for(int i=0;i<visible.size();i++)options.setItemChecked(i,chosen.containsKey(visible.get(i).id));count.setText(chosen.size()+"/"+FootballTeams.MAX_FAVORITES+" favoritos · "+visible.size()+" resultados");};
  options.setOnItemClickListener((parent,view,index,id)->{FootballTeams.Team team=visible.get(index);if(chosen.containsKey(team.id))chosen.remove(team.id);else if(chosen.size()<FootballTeams.MAX_FAVORITES)chosen.put(team.id,team);else{options.setItemChecked(index,false);count.setText("Máximo cinco: desmarca un favorito para añadir otro");return;}count.setText(chosen.size()+"/"+FootballTeams.MAX_FAVORITES+" favoritos · "+visible.size()+" resultados");});
  search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence text,int start,int before,int after){}public void onTextChanged(CharSequence text,int start,int before,int after){refresh.run();}public void afterTextChanged(Editable text){}});
  teamDialog=new AlertDialog.Builder(home).setTitle("Mis equipos de fútbol").setView(body).setPositiveButton("Guardar",null).setNegativeButton("Cancelar",null).setNeutralButton("Originales",null).create();
  teamDialog.setOnShowListener(d->{teamDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(v->{if(chosen.isEmpty()){count.setText("Elige al menos un equipo antes de guardar");return;}saveTeams(chosen.values());teamDialog.dismiss();});teamDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v->{chosen.clear();for(FootballTeams.Team t:catalog.defaults())chosen.put(t.id,t);refresh.run();});refresh.run();options.requestFocus();});
  teamDialog.setOnDismissListener(d->{teamDialog=null;if(active)teamsButton.requestFocus();});teamDialog.show();teamDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 }

}
