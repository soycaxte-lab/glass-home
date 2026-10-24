package local.glasshome;

import android.content.*;
import android.content.pm.PackageManager;
import android.database.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.Uri;
import android.os.*;
import android.util.LruCache;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/** Read-only view of searchable Watch Next entries published by installed streaming apps. */
final class WatchNextRow extends LinearLayout {
 private static final Uri URI=Uri.parse("content://android.media.tv/watch_next_program");
 private static final String[] COLUMNS={"_id","package_name","title","intent_uri","watch_next_type","last_playback_position_millis","duration_millis","poster_art_uri","last_engagement_time_utc_millis","browsable","type"};
 private final MainActivity home;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final ExecutorService images=Executors.newFixedThreadPool(2);
 private final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(4*1024*1024){protected int sizeOf(String key,Bitmap b){return b.getAllocationByteCount();}};
 private final ArrayList<Entry> entries=new ArrayList<>();
 private final ArrayList<Entry> loaded=new ArrayList<>();
 private int filter=0; private String lastError;
 private final ArrayList<View> cards=new ArrayList<>();
 private final LinearLayout row;
 private final HorizontalScrollView scroller;
 private final TextView message;
 private boolean active=false,registered=false;
 private int generation=0;
 private String returnKey=null;
 private final Runnable reload=this::load;
 private final ContentObserver observer=new ContentObserver(handler){public void onChange(boolean self){handler.removeCallbacks(reload);handler.postDelayed(reload,350);}};
 private static final class Entry {String key,pkg,title,art;Intent intent;long position,duration,time;int type=-1;}
 WatchNextRow(MainActivity h){
  super(h);home=h;setOrientation(VERTICAL);setPadding(0,h.dp(3),0,h.dp(3));setClipChildren(false);
  scroller=new HorizontalScrollView(h);scroller.setHorizontalScrollBarEnabled(false);scroller.setClipToPadding(false);scroller.setClipChildren(false);scroller.setFocusable(false);scroller.getViewTreeObserver().addOnScrollChangedListener(()->{View f=home.getCurrentFocus();if(isCard(f))home.moveGlass(f);});addView(scroller,new LayoutParams(-1,0,1));
  row=new LinearLayout(h);row.setPadding(h.dp(6),h.dp(5),h.dp(6),h.dp(4));row.setClipChildren(false);scroller.addView(row,new HorizontalScrollView.LayoutParams(-2,-1));
  message=h.text("Cargando tus títulos…",12,0xffcedbf1);message.setPadding(h.dp(18),h.dp(16),h.dp(18),h.dp(16));message.setBackground(h.surface(20,false,0x30465483));row.addView(message);
 }
 void start(){active=true;if(!registered){home.getContentResolver().registerContentObserver(URI,true,observer);registered=true;}load();}
 void stop(){active=false;generation++;handler.removeCallbacks(reload);if(registered){home.getContentResolver().unregisterContentObserver(observer);registered=false;}}
 void destroy(){stop();worker.shutdownNow();images.shutdownNow();cache.evictAll();}
 boolean restorePreferredFocus(){if(returnKey==null)return false;for(int i=0;i<entries.size();i++)if(entries.get(i).key.equals(returnKey)){cards.get(i).post(()->{for(int j=0;j<entries.size();j++)if(entries.get(j).key.equals(returnKey))cards.get(j).requestFocus();});return true;}return false;}
 void clearReturnFocus(){returnKey=null;}
 boolean isCard(View v){return cards.contains(v);}
 boolean focusCard(int index){if(cards.isEmpty())return false;cards.get(Math.min(index,cards.size()-1)).requestFocus();return true;}
 void setFilter(int value){filter=value;returnKey=null;renderFiltered(generation);}
 private void renderFiltered(int gen){ArrayList<Entry> shown=new ArrayList<>();for(Entry e:loaded)if(filter==0||(filter==1&&e.type==android.media.tv.TvContract.WatchNextPrograms.TYPE_MOVIE)||(filter==2&&e.type>=android.media.tv.TvContract.WatchNextPrograms.TYPE_TV_SERIES&&e.type<=android.media.tv.TvContract.WatchNextPrograms.TYPE_TV_EPISODE))shown.add(e);render(shown,lastError,gen);}
 private void load(){
  if(!active)return;final int gen=++generation;
  worker.execute(()->{ArrayList<Entry> result=new ArrayList<>();String error=null;
   if(home.checkSelfPermission("android.permission.READ_TV_LISTINGS")!=PackageManager.PERMISSION_GRANTED)error="Permite el acceso a los listados de TV para ver tus continuaciones.";
   else try(Cursor c=home.getContentResolver().query(URI,COLUMNS,null,null,null)){
    if(c!=null)while(c.moveToNext()){
     if(c.getInt(4)!=0||c.getInt(9)==0)continue;
     String pkg=c.getString(1),link=c.getString(3);if(pkg==null||link==null||c.getLong(5)<=0)continue;
     if(home.getPackageManager().getLeanbackLaunchIntentForPackage(pkg)==null&&home.getPackageManager().getLaunchIntentForPackage(pkg)==null)continue;
     Intent intent=Intent.parseUri(link,Intent.URI_INTENT_SCHEME);
     // The publisher remains the only app allowed to receive its resume link.
     intent.setSelector(null);intent.setPackage(pkg);intent.setClipData(null);intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     if(intent.getComponent()!=null&&!pkg.equals(intent.getComponent().getPackageName()))continue;
     if(intent.getAction()==null)intent.setAction(Intent.ACTION_VIEW);
     Entry e=new Entry();e.key=pkg+":"+c.getLong(0);e.pkg=pkg;e.title=c.getString(2);e.intent=intent;e.position=c.getLong(5);e.duration=c.getLong(6);e.art=c.getString(7);e.time=c.getLong(8);e.type=c.isNull(10)?-1:c.getInt(10);
     if(e.title==null||e.title.trim().isEmpty())continue;result.add(e);
    }
   }catch(Exception e){android.util.Log.w("GlassWatchNext","Cannot read shared titles",e);error="No se pudo cargar Continuar viendo en este momento.";}
   result.sort((a,b)->Long.compare(b.time,a.time));if(result.size()>12)result.subList(12,result.size()).clear();
   final String status=error;
   handler.post(()->{if(!active||gen!=generation)return;loaded.clear();loaded.addAll(result);lastError=status;renderFiltered(gen);});
  });
 }
 private void render(ArrayList<Entry> items,String error,int gen){
  View focused=home.getCurrentFocus();String key=returnKey;int old=cards.indexOf(focused);if(old>=0&&old<entries.size())key=entries.get(old).key;
  final String focusKey=key;entries.clear();entries.addAll(items);cards.clear();row.removeAllViews();
  if(items.isEmpty()){message.setText(error!=null?error:filter==0?"Tu próxima historia empieza aquí.\nReproduce un título en una app compatible para continuar desde el inicio.":"No hay "+(filter==1?"películas":"series")+" compartidas para continuar.\nExplorar muestra todos los títulos disponibles.");row.addView(message);return;}
  for(int i=0;i<items.size();i++){
   Entry e=items.get(i);final int index=i;
   boolean featured=false;
   int available=(int)((home.contentWidthDp()-20)*.46f);
   int width=Math.max(120,Math.min(available-24,286));
   LinearLayout card=new LinearLayout(home);card.setOrientation(VERTICAL);card.setClipChildren(false);card.setFocusable(true);card.setClickable(true);
   LayoutParams cp=new LayoutParams(home.dp(width),-1);cp.rightMargin=i==items.size()-1?0:home.dp(10);row.addView(card,cp);cards.add(card);
   FrameLayout poster=new FrameLayout(home);poster.setBackground(home.shape(0xff263658,9));poster.setClipToOutline(true);card.addView(poster,new LayoutParams(-1,0,1));
   ImageView image=new ImageView(home);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setBackgroundColor(0xff263658);poster.addView(image,new FrameLayout.LayoutParams(-1,-1));
   try{Drawable banner=home.getPackageManager().getApplicationBanner(e.pkg);if(banner==null)banner=home.getPackageManager().getApplicationIcon(e.pkg);image.setImageDrawable(banner);}catch(Exception ignored){}
   LinearLayout details=new LinearLayout(home);details.setOrientation(VERTICAL);details.setPadding(home.dp(2),home.dp(5),home.dp(2),0);card.addView(details,new LayoutParams(-1,home.dp(39)));
   TextView label=home.text(e.title,featured?13:12,0xfff5f8ff);label.setTypeface(home.font(550));label.setFontVariationSettings("'wght' 550");label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);details.addView(label,new LayoutParams(-1,home.dp(17)));
   String appName=e.pkg.equals("com.netflix.ninja")?"Netflix":e.pkg.equals("com.amazon.amazonvideo.livingroom")?"Prime Video":e.pkg.equals("com.teveo.tv.v04")?"TEVEO":e.pkg;
   TextView source=home.text("Continuar · "+appName,10,0xffd0dff8);source.setSingleLine(true);source.setEllipsize(android.text.TextUtils.TruncateAt.END);details.addView(source,new LayoutParams(-1,home.dp(15)));
   ProgressBar progress=new ProgressBar(home,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgress(e.duration>0?(int)Math.min(1000,e.position*1000/e.duration):0);progress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffbcebf5));FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(-1,home.dp(2),Gravity.BOTTOM);pp.leftMargin=home.dp(8);pp.rightMargin=home.dp(8);pp.bottomMargin=home.dp(6);poster.addView(progress,pp);if(e.duration<=0)progress.setVisibility(GONE);
   View border=new View(home);border.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);border.setBackground(home.surface(9,false,0x013c4863));poster.addView(border,new FrameLayout.LayoutParams(-1,-1));
   card.setContentDescription(e.title+", "+appName+(e.duration>0?", "+(e.position*100/e.duration)+" por ciento visto":"")+", continuar reproducción");
   card.setOnClickListener(v->{returnKey=e.key;home.open(new Intent(e.intent));});
   card.setOnFocusChangeListener((v,f)->{v.animate().scaleX(f?1.025f:1).scaleY(f?1.025f:1).setDuration(180).start();if(f){home.scroll.smoothScrollTo(0,0);handler.post(()->{home.moveGlass(v);scroller.smoothScrollTo(Math.max(0,v.getLeft()-home.dp(8)),0);});}});
   card.setOnKeyListener((v,k,event)->{if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==KeyEvent.KEYCODE_DPAD_UP){returnKey=null;home.focusCategory();return true;}if(k==KeyEvent.KEYCODE_DPAD_DOWN){returnKey=null;if(!home.dockTiles.isEmpty())home.dockTiles.get(Math.min(index,home.dockTiles.size()-1)).requestFocus();return true;}if(k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_DPAD_RIGHT){int n=index+(k==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);if(n>=0&&n<cards.size())cards.get(n).requestFocus();else if(n<0)home.recommendation.focusCard();return true;}return false;});
   if(e.art!=null){Bitmap bitmap=cache.get(e.art);if(bitmap!=null)showArtwork(card,image,bitmap);else images.execute(()->{Bitmap b=artwork(e.art);if(b==null)return;cache.put(e.art,b);handler.post(()->{if(active&&generation==gen)showArtwork(card,image,b);});});}
   if(e.key.equals(focusKey))card.post(()->{if(active&&generation==gen)card.requestFocus();});
  }
 }
 private void showArtwork(View card,ImageView image,Bitmap bitmap){
  image.setImageBitmap(bitmap);
  image.addOnLayoutChangeListener((v,l,t,r,b,ol,ot,or,ob)->{if(b-t!=ob-ot)fitArtwork(card,image,bitmap);});
  image.post(()->fitArtwork(card,image,bitmap));
 }
 private void fitArtwork(View card,ImageView image,Bitmap bitmap){
  if(bitmap.isRecycled()||image.getHeight()<=0||getWidth()<=0)return;
  float ratio=(float)bitmap.getWidth()/Math.max(1,bitmap.getHeight());
  int available=Math.max(home.dp(110),getWidth()-home.dp(24));
  int width=Math.max(home.dp(110),Math.min(available,Math.round(image.getHeight()*ratio)));
  LayoutParams params=(LayoutParams)card.getLayoutParams();
  if(params.width!=width){params.width=width;card.setLayoutParams(params);}
  if(card.hasFocus())card.post(()->{home.moveGlass(card);scroller.smoothScrollTo(Math.max(0,card.getLeft()-home.dp(8)),0);});
 }
 private InputStream stream(String link)throws IOException{
  Uri uri=Uri.parse(link);String scheme=uri.getScheme();
  if("content".equals(scheme)||"android.resource".equals(scheme))return home.getContentResolver().openInputStream(uri);
  if(!"https".equals(scheme))return null;
  HttpURLConnection connection=(HttpURLConnection)new URL(link).openConnection();connection.setConnectTimeout(3500);connection.setReadTimeout(3500);connection.setInstanceFollowRedirects(false);
  int code=connection.getResponseCode();if(code!=200){connection.disconnect();return null;}
  InputStream original=connection.getInputStream();return new FilterInputStream(original){public void close()throws IOException{try{super.close();}finally{connection.disconnect();}}};
 }
 private Bitmap artwork(String uri){
  try(InputStream in=stream(uri)){if(in==null)return null;ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))!=-1){if(data.size()+n>2*1024*1024)return null;data.write(bytes,0,n);}byte[] body=data.toByteArray();BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(body,0,body.length,options);options.inSampleSize=1;while(options.outWidth/options.inSampleSize>960||options.outHeight/options.inSampleSize>720)options.inSampleSize*=2;options.inJustDecodeBounds=false;options.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeByteArray(body,0,body.length,options);}catch(Exception ignored){return null;}
 }
}

