package local.glasshome;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.*;
import android.graphics.drawable.*;
import android.net.*;
import android.net.wifi.*;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.view.animation.OvershootInterpolator;
import android.widget.*;
import androidx.recyclerview.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 static class App { String name,pkg; Intent intent; Drawable art,icon; int color; App(String n,String p,Intent i,Drawable a,int c){name=n;pkg=p;intent=i;art=a;icon=a;color=c;} }
 final ArrayList<App> all=new ArrayList<>(),dock=new ArrayList<>(),others=new ArrayList<>();
 final ArrayList<Tile> dockTiles=new ArrayList<>();
 final ExecutorService loader=Executors.newSingleThreadExecutor();
 FrameLayout root; LinearLayout page,top; RecyclerView grid; ScrollView scroll; TextView title,wifi; Glow glow; View glass; WatchNextRow watch; FootballRow football; RecommendationPanel recommendation;
 int columns,lastDock=0; String lastPackage=""; boolean alive=true,resumed=false,appsDirty=true,appsLoading=false,appsLoaded=false; int generation=0;
 final HashMap<Integer,Typeface> fonts=new HashMap<>();
 final int[] tileLocation=new int[2],rootLocation=new int[2];
 float glassTargetX=Float.NaN,glassTargetY=Float.NaN;
 final Adapter appAdapter=new Adapter();
 final Runnable secondaryLoad=()->{if(alive&&resumed){watch.start();football.start();recommendation.start();}};
 final BroadcastReceiver packages=new BroadcastReceiver(){public void onReceive(Context c,Intent i){appsDirty=true;if(resumed)load();}};
 TextView dateTime,greeting; final Handler clockHandler=new Handler(Looper.getMainLooper());
 LinearLayout categories; final TextView[] categoryButtons=new TextView[3]; int activeCategory=0;
 final Runnable clockTick=new Runnable(){public void run(){updateClock();clockHandler.postDelayed(this,60000-System.currentTimeMillis()%60000);}};
 Typeface font(int weight){Typeface cached=fonts.get(weight);if(cached==null){Typeface base=getResources().getFont(R.font.outfit);cached=Build.VERSION.SDK_INT>=28?Typeface.create(base,weight,false):base;fonts.put(weight,cached);}return cached;}
 int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
 TextView text(String s,float size,int color){TextView t=new TextView(this);t.setText(s);t.setTypeface(font(450));t.setFontVariationSettings("'wght' 450");t.setTextSize(size);t.setTextColor(color);t.setIncludeFontPadding(false);return t;}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 LiquidSurface surface(int radius,boolean focus,int tint){return new LiquidSurface(getResources().getDisplayMetrics().density,radius,focus,tint);}
 @Override public void onCreate(Bundle b){super.onCreate(b);columns=Math.max(4,Math.min(7,getResources().getInteger(R.integer.columns)));lastPackage=getPreferences(0).getString("last","");window();build();IntentFilter f=new IntentFilter();f.addAction(Intent.ACTION_PACKAGE_ADDED);f.addAction(Intent.ACTION_PACKAGE_REMOVED);f.addAction(Intent.ACTION_PACKAGE_CHANGED);f.addAction(Intent.ACTION_PACKAGE_REPLACED);f.addDataScheme("package");if(Build.VERSION.SDK_INT>=33)registerReceiver(packages,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(packages,f);}
 void window(){getWindow().getDecorView().setSystemUiVisibility(5894);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);}
 @Override public void onResume(){super.onResume();resumed=true;window();clockHandler.removeCallbacks(clockTick);clockTick.run();load();if(appsLoaded)watch.restorePreferredFocus();root.removeCallbacks(secondaryLoad);root.postOnAnimation(secondaryLoad);}
 @Override public void onPause(){resumed=false;root.removeCallbacks(secondaryLoad);clockHandler.removeCallbacks(clockTick);watch.stop();football.stop();recommendation.stop();super.onPause();}
 @Override public void onDestroy(){alive=false;unregisterReceiver(packages);root.removeCallbacks(secondaryLoad);generation++;clockHandler.removeCallbacks(clockTick);loader.shutdownNow();watch.destroy();football.destroy();recommendation.destroy();super.onDestroy();}
 @Override public void onBackPressed(){watch.clearReturnFocus();if(dockTiles.isEmpty())return;if(scroll.getScrollY()>0){scroll.smoothScrollTo(0,0);dockTiles.get(lastDock).requestFocus();}else dockTiles.get(lastDock).requestFocus();}
 int contentWidthDp(){return (int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density)-86;}
 void build(){
 root=new FrameLayout(this);root.setBackgroundColor(getColor(R.color.wallpaper));setContentView(root);
 glow=new Glow();root.addView(glow,new FrameLayout.LayoutParams(-1,-1));
 scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipChildren(false);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
 page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(52),dp(12),dp(34),dp(20));page.setClipChildren(false);page.setClipToPadding(false);scroll.addView(page);
 int screenH=(int)(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density);
 LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setClipChildren(false);page.addView(hero,new LinearLayout.LayoutParams(-1,dp(Math.max(480,screenH-24))));
 FrameLayout header=new FrameLayout(this);hero.addView(header,new LinearLayout.LayoutParams(-1,dp(132)));
 LinearLayout welcome=new LinearLayout(this);welcome.setOrientation(LinearLayout.VERTICAL);FrameLayout.LayoutParams wp=new FrameLayout.LayoutParams(dp(Math.max(220,Math.min(316,contentWidthDp()/2-116))),-2,Gravity.TOP|Gravity.LEFT);wp.topMargin=dp(31);header.addView(welcome,wp);
 greeting=text("¡Hola!",12,0xffe1eaff);welcome.addView(greeting);
 TextView question=text("¿Qué quieres ver hoy?",21,Color.WHITE);question.setTypeface(font(550));question.setFontVariationSettings("'wght' 550");question.setSingleLine(true);LinearLayout.LayoutParams qp=new LinearLayout.LayoutParams(-1,-2);qp.topMargin=dp(4);welcome.addView(question,qp);
 dateTime=text("",11,0xfff1f4ff);dateTime.setGravity(Gravity.CENTER);dateTime.setBackground(new LayerDrawable(new Drawable[]{shape(0x70303b83,16),surface(16,false,0x802c367a)}));header.addView(dateTime,new FrameLayout.LayoutParams(dp(170),dp(28),Gravity.TOP|Gravity.CENTER_HORIZONTAL));
 football=new FootballRow(this);FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(dp(224),dp(92),Gravity.TOP|Gravity.CENTER_HORIZONTAL);fp.topMargin=dp(35);header.addView(football,fp);
 top=new LinearLayout(this);top.setGravity(Gravity.CENTER);FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-2,dp(40),Gravity.RIGHT|Gravity.TOP);tp.topMargin=dp(32);header.addView(top,tp);
 wifi=pill("",()->open(new Intent(Settings.ACTION_WIFI_SETTINGS)));wifi.setPadding(dp(10),0,dp(10),0);wifi.setCompoundDrawablesWithIntrinsicBounds(new WifiIcon(),null,null,null);wifi.setContentDescription("Abrir ajustes de Wi-Fi");
 TextView soccer=pill("⚽",()->football.focusTab());soccer.setPadding(dp(10),0,dp(10),0);soccer.setContentDescription("Ir al widget de fútbol");TextView settings=pill("⚙  Ajustes",()->open(new Intent(Settings.ACTION_SETTINGS)));top.addView(wifi);top.addView(soccer);top.addView(settings);
 for(int i=0;i<top.getChildCount();i++){final int idx=i;top.getChildAt(i).setOnKeyListener((x,k,e)->{if(e.getAction()!=0)return false;if(k==20){if(!watch.focusCard(0))recommendation.focusCard();return true;}if(k==19)return true;if(k==21||k==22){int n=idx+(k==21?-1:1);if(n>=0&&n<top.getChildCount())top.getChildAt(n).requestFocus();else if(n<0)football.focusTab();return true;}return false;});}
 categories=new LinearLayout(this);categories.setGravity(Gravity.CENTER_VERTICAL);FrameLayout.LayoutParams cats=new FrameLayout.LayoutParams(-2,dp(30),Gravity.LEFT|Gravity.TOP);cats.topMargin=dp(88);header.addView(categories,cats);
 String[] names={"Explorar","Películas","Series"};
 for(int i=0;i<3;i++){final int index=i;TextView chip=text(names[i],12,Color.WHITE);chip.setGravity(Gravity.CENTER);chip.setPadding(dp(14),0,dp(14),0);chip.setFocusable(true);chip.setClickable(true);categoryButtons[i]=chip;LinearLayout.LayoutParams clp=new LinearLayout.LayoutParams(-2,dp(27));clp.rightMargin=dp(8);categories.addView(chip,clp);chip.setOnClickListener(v->{activeCategory=index;watch.setFilter(index);recommendation.setFilter(index);styleCategories();});chip.setOnFocusChangeListener((v,f)->{styleCategories();if(f){glass.setVisibility(View.INVISIBLE);scroll.smoothScrollTo(0,0);}});chip.setOnKeyListener((v,k,e)->{if(e.getAction()!=0)return false;if(k==19){football.focusTab();return true;}if(k==20){recommendation.focusCard();return true;}if(k==21||k==22){int n=index+(k==21?-1:1);if(n>=0&&n<3)categoryButtons[n].requestFocus();return true;}return false;});}
 styleCategories();
 LinearLayout media=new LinearLayout(this);media.setClipChildren(false);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,0,1);mp.topMargin=dp(4);mp.bottomMargin=dp(10);hero.addView(media,mp);
 recommendation=new RecommendationPanel(this);LinearLayout.LayoutParams recParams=new LinearLayout.LayoutParams(0,-1,.54f);recParams.rightMargin=dp(20);media.addView(recommendation,recParams);
 LinearLayout watching=new LinearLayout(this);watching.setOrientation(LinearLayout.VERTICAL);watching.setClipChildren(false);media.addView(watching,new LinearLayout.LayoutParams(0,-1,.46f));watching.addView(text("Continuar viendo",15,Color.WHITE),new LinearLayout.LayoutParams(-1,dp(22)));watch=new WatchNextRow(this);watching.addView(watch,new LinearLayout.LayoutParams(-1,0,1));
 title=text("",12,Color.WHITE);title.setVisibility(View.GONE);hero.addView(title,new LinearLayout.LayoutParams(-1,0));
 FrameLayout dockArea=new FrameLayout(this);dockArea.setClipChildren(false);hero.addView(dockArea,new LinearLayout.LayoutParams(-1,dp(104)));
 FrameLayout band=new FrameLayout(this);band.setBackground(surface(28,false,0x355c76bd));band.setClipChildren(false);dockArea.addView(band,new FrameLayout.LayoutParams(dp(Math.min(620,contentWidthDp())), -1,Gravity.CENTER));
 glass=new View(this);glass.setBackground(surface(12,true,0x03536e98));glass.setVisibility(View.INVISIBLE);glass.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);root.addView(glass,new FrameLayout.LayoutParams(1,1));scroll.getViewTreeObserver().addOnScrollChangedListener(()->{View f=getCurrentFocus();if(f instanceof Tile&&!((Tile)f).isDock||watch.isCard(f))moveGlass(f);});
 LinearLayout dockRow=new LinearLayout(this);dockRow.setTag("dock");dockRow.setPadding(dp(12),dp(4),dp(12),dp(4));dockRow.setClipChildren(false);band.addView(dockRow,new FrameLayout.LayoutParams(-1,-1));
 TextView hint=text("Todas las apps   ↓",10,0xffe0e9ff);hint.setGravity(Gravity.CENTER);hero.addView(hint,new LinearLayout.LayoutParams(-1,dp(24)));
 TextView appsLabel=text("Aplicaciones",22,Color.WHITE);appsLabel.setPadding(0,dp(24),0,dp(18));page.addView(appsLabel);
 grid=new RecyclerView(this);grid.setNestedScrollingEnabled(false);grid.setClipChildren(false);grid.setClipToPadding(false);grid.setLayoutManager(new GridLayoutManager(this,columns));grid.setItemAnimator(null);grid.setAdapter(appAdapter);page.addView(grid,new LinearLayout.LayoutParams(-1,-2));
 }
 class WifiIcon extends Drawable {
  final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  public int getIntrinsicWidth(){return dp(20);}public int getIntrinsicHeight(){return dp(20);}
  public void draw(Canvas c){Rect b=getBounds();float w=b.width(),cx=b.exactCenterX(),cy=b.top+w*.70f;p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(dp(1.6f));for(float r:new float[]{w*.65f,w*.44f,w*.23f})c.drawArc(cx-r,cy-r,cx+r,cy+r,225,90,false,p);p.setStyle(Paint.Style.FILL);c.drawCircle(cx,cy,dp(1.2f),p);}
  public void setAlpha(int a){}public void setColorFilter(ColorFilter f){}public int getOpacity(){return PixelFormat.TRANSLUCENT;}
 }
 void focusCategory(){categoryButtons[activeCategory].requestFocus();}
 void styleCategories(){for(int i=0;i<3;i++){TextView chip=categoryButtons[i];if(chip==null)continue;chip.setBackground(surface(14,chip.hasFocus(),i==activeCategory?0x806e8bc3:0x80304067));chip.setTextColor(i==activeCategory?Color.WHITE:0xffc7d7f1);chip.setSelected(i==activeCategory);}}
 void updateClock(){
  java.time.ZonedDateTime now=java.time.ZonedDateTime.now();
  String format=android.text.format.DateFormat.is24HourFormat(this)?"EEE d MMM · HH:mm":"EEE d MMM · h:mm a";
  dateTime.setText(now.format(java.time.format.DateTimeFormatter.ofPattern(format,java.util.Locale.forLanguageTag("es"))));
  int hour=now.getHour();greeting.setText("¡Hola!");
 }
 TextView pill(String label,Runnable action){TextView t=text(label,12,0xffe5e5ed);t.setGravity(Gravity.CENTER);t.setPadding(dp(16),dp(10),dp(16),dp(10));t.setBackground(new LayerDrawable(new Drawable[]{shape(0x80425370,24),surface(24,false,0x354b5670)}));t.setFocusable(true);t.setClickable(true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(40));lp.leftMargin=dp(8);t.setLayoutParams(lp);t.setOnClickListener(v->action.run());t.setOnFocusChangeListener((v,f)->{v.setBackground(new LayerDrawable(new Drawable[]{shape(0x80425370,24),surface(24,f,0x354b5670)}));t.setTextColor(f?Color.WHITE:0xffe5edff);v.animate().scaleX(f?1.04f:1).scaleY(f?1.04f:1).setDuration(180).start();if(f)glass.setVisibility(View.INVISIBLE);});return t;}
 void open(Intent i){try{startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception e){Toast.makeText(this,"Esta opción no está disponible en este dispositivo",Toast.LENGTH_SHORT).show();}}
 void load(){if(!appsDirty||appsLoading||!alive)return;appsDirty=false;appsLoading=true;final int gen=++generation;loader.execute(()->{try{ArrayList<App> found=new ArrayList<>();HashSet<String> seen=new HashSet<>();PackageManager pm=getPackageManager();
 for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){Intent q=new Intent(Intent.ACTION_MAIN).addCategory(category);for(ResolveInfo ri:pm.queryIntentActivities(q,0)){String p=ri.activityInfo.packageName;if(p.equals(getPackageName())||!seen.add(p))continue;Intent launch=new Intent(Intent.ACTION_MAIN).addCategory(category).setComponent(new ComponentName(p,ri.activityInfo.name));Drawable art=ri.activityInfo.loadBanner(pm);if(art==null)art=ri.loadIcon(pm);String name=ri.loadLabel(pm).toString();if(p.equals("com.teveo.tv.v04"))name="TEVEO";int color=sample(art);App app=new App(name,p,launch,art,color);app.icon=ri.loadIcon(pm);found.add(app);}}
 found.sort((a,b)->a.name.compareToIgnoreCase(b.name));runOnUiThread(()->{if(!alive||gen!=generation)return;appsLoading=false;all.clear();all.addAll(found);appsLoaded=true;render();if(appsDirty&&resumed)load();});}catch(Exception ex){runOnUiThread(()->{appsLoading=false;appsDirty=true;android.util.Log.w("GlassApps","App list unavailable",ex);});}});
 // Keep the control compact and avoid displaying private network names on Home.
 wifi.setText("");
 }
 int sample(Drawable d){try{Bitmap b=Bitmap.createBitmap(24,14,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);d.setBounds(0,0,24,14);d.draw(c);int r=0,g=0,bl=0,n=0;for(int y=0;y<14;y++)for(int x=0;x<24;x++){int v=b.getPixel(x,y);if(Math.max(Color.red(v),Math.max(Color.green(v),Color.blue(v)))-Math.min(Color.red(v),Math.min(Color.green(v),Color.blue(v)))>45){r+=Color.red(v);g+=Color.green(v);bl+=Color.blue(v);n++;}}b.recycle();return n==0?0xff384080:Color.rgb(r/n,g/n,bl/n);}catch(Exception e){return 0xff384080;}}
 void render(){dock.clear();others.clear();dockTiles.clear();for(String p:getResources().getStringArray(R.array.dock_apps))for(App a:all)if(a.pkg.equals(p)&&dock.size()<columns)dock.add(a);for(App a:all)if(!dock.contains(a))others.add(a);
 LinearLayout row=page.findViewWithTag("dock");row.removeAllViews();for(int i=0;i<dock.size();i++){Tile t=new Tile(dock.get(i),i,true);dockTiles.add(t);row.addView(t,new LinearLayout.LayoutParams(0,-1,1));}appAdapter.notifyDataSetChanged();
 if(watch.restorePreferredFocus())return; for(int i=0;i<others.size();i++){if(others.get(i).pkg.equals(lastPackage)){final int pos=i;grid.post(()->focusGrid(pos));return;}} View target=null;for(Tile t:dockTiles)if(t.app.pkg.equals(lastPackage))target=t;if(target==null&&!dockTiles.isEmpty())target=dockTiles.get(Math.min(lastDock,dockTiles.size()-1));if(target!=null){View finalTarget=target;target.post(()->finalTarget.requestFocus());}}
 class Tile extends LinearLayout {App app;int index;boolean isDock;ImageView image;TextView label;
 Tile(App a,int i,boolean d){super(MainActivity.this);app=a;index=i;isDock=d;setOrientation(VERTICAL);setGravity(Gravity.CENTER);setPadding(dp(8),dp(2),dp(8),dp(2));setFocusable(true);setContentDescription(a==null?"":a.name);setClickable(true);setClipChildren(false);
 image=new ImageView(MainActivity.this);if(a!=null)image.setImageDrawable(d?a.icon:a.art);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setBackground(surface(d?19:14,false,0x183e4e6e));image.setClipToOutline(true);image.setPadding(dp(d?3:8),dp(d?3:6),dp(d?3:8),dp(d?3:6));addView(image,new LinearLayout.LayoutParams(d?dp(76):-1,dp(d?62:77)));if(d&&a!=null)styleDockIcon(image,a,false);
 label=text(a==null?"":a.name,d?11:12,0xffd6d6de);label.setGravity(Gravity.CENTER);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(18));lp.topMargin=dp(5);addView(label,lp);
 setOnClickListener(v->{lastPackage=app.pkg;open(app.intent);});setOnFocusChangeListener((v,f)->{animate().scaleX(f?(isDock?1.09f:1.035f):1).scaleY(f?(isDock?1.09f:1.035f):1).translationY(f&&isDock?-dp(2):0).setDuration(240).setInterpolator(new OvershootInterpolator(.8f)).start();label.setTextColor(f?Color.WHITE:0xffdce6ff);if(isDock)styleDockIcon(image,app,f);if(f){if(!app.pkg.equals(lastPackage)){lastPackage=app.pkg;getPreferences(0).edit().putString("last",lastPackage).apply();}if(isDock){lastDock=index;title.setText("Tus apps · "+app.name);glow.setColor(app.color);scroll.smoothScrollTo(0,0);glass.setVisibility(View.INVISIBLE);}else{post(()->moveGlass(this));scroll.post(()->{int[] pos=new int[2];getLocationOnScreen(pos);if(pos[1]+getHeight()>root.getHeight()-dp(25))scroll.smoothScrollBy(0,pos[1]+getHeight()-root.getHeight()+dp(35));});}}});
 setOnKeyListener((v,k,e)->{if(e.getAction()!=0)return false;if(k!=19&&k!=20&&k!=21&&k!=22)return false;
 if(isDock){if(k==19){if(index==0)recommendation.focusCard();else if(!watch.focusCard(index-1))recommendation.focusCard();return true;}if(k==20){focusGrid(Math.min(index,others.size()-1));return true;}int next=index+(k==21?-1:1);if(next>=0&&next<dockTiles.size())dockTiles.get(next).requestFocus();return true;}
 int next=index;if(k==19){if(index<columns){dockTiles.get(Math.min(index,dockTiles.size()-1)).requestFocus();return true;}next-=columns;}if(k==20)next+=columns;if(k==21&&index%columns>0)next--;if(k==22&&index%columns<columns-1)next++;if(next>=0&&next<others.size())focusGrid(next);return true;});
 }
 }
 void styleDockIcon(ImageView image,App app,boolean focused){int color=app.pkg.equals("com.netflix.ninja")||app.pkg.equals("com.google.android.youtube.tv")?0xfff6f8ff:app.pkg.equals("com.teveo.tv.v04")?0xff111535:0xff087be5;GradientDrawable bg=shape(color,16);if(focused)bg.setStroke(dp(2),0xffeef9ff);image.setBackground(bg);image.setElevation(dp(focused?9:2));}
 void moveGlass(View tile){FrameLayout band=(FrameLayout)glass.getParent();int[] a=tileLocation,b=rootLocation;tile.getLocationInWindow(a);band.getLocationInWindow(b);FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)glass.getLayoutParams();if(lp.width!=tile.getWidth()||lp.height!=tile.getHeight()){lp.width=tile.getWidth();lp.height=tile.getHeight();glass.setLayoutParams(lp);}float x=a[0]-b[0],y=a[1]-b[1];if(glass.getVisibility()==View.VISIBLE&&glassTargetX==x&&glassTargetY==y)return;glassTargetX=x;glassTargetY=y;if(glass.getVisibility()!=View.VISIBLE){glass.setTranslationX(x);glass.setTranslationY(y);glass.setVisibility(View.VISIBLE);}else glass.animate().translationX(x).translationY(y).setDuration(240).setInterpolator(new OvershootInterpolator(.7f)).start();}
 void focusGrid(int n){if(n<0||n>=others.size())return;RecyclerView.ViewHolder h=grid.findViewHolderForAdapterPosition(n);if(h!=null)h.itemView.requestFocus();else{grid.scrollToPosition(n);grid.post(()->{RecyclerView.ViewHolder vh=grid.findViewHolderForAdapterPosition(n);if(vh!=null)vh.itemView.requestFocus();});}}
 void bindTile(Tile tile,App app,int position){tile.app=app;tile.index=position;tile.setContentDescription(app.name);tile.image.setImageDrawable(app.art);tile.label.setText(app.name);tile.animate().cancel();tile.setScaleX(tile.hasFocus()?1.035f:1);tile.setScaleY(tile.hasFocus()?1.035f:1);tile.label.setTextColor(tile.hasFocus()?Color.WHITE:0xffdce6ff);}
 class Holder extends RecyclerView.ViewHolder{Holder(View v){super(v);}}
 class Adapter extends RecyclerView.Adapter<Holder>{@Override public int getItemCount(){return others.size();}@Override public Holder onCreateViewHolder(ViewGroup parent,int type){Tile t=new Tile(null,0,false);t.setLayoutParams(new RecyclerView.LayoutParams(-1,dp(125)));return new Holder(t);}@Override public void onBindViewHolder(Holder h,int pos){bindTile((Tile)h.itemView,others.get(pos),pos);} }
 class Glow extends View {
  final Paint paint=new Paint(3);final Rect target=new Rect();Shader accent;int color=0xff6e67b8;Bitmap backdrop;int width,height;
  Glow(){super(MainActivity.this);paint.setDither(true);}
  void setColor(int value){if(color==value)return;color=value;accentShader();invalidate();} void accentShader(){if(width>0)accent=new RadialGradient(width*.33f,height*.77f,width*.42f,(color&0xffffff)|0x18000000,Color.TRANSPARENT,Shader.TileMode.CLAMP);}
  protected void onSizeChanged(int w,int h,int ow,int oh){width=w;height=h;target.set(0,0,w,h);accentShader();if(backdrop!=null)backdrop.recycle();backdrop=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(backdrop);
   paint.setShader(new LinearGradient(0,0,960,540,new int[]{0xff073cc0,0xff213cb7,0xff508cde},new float[]{0,.5f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,paint);
   light(c,470,50,300,0xdd9270f6);light(c,270,480,430,0xccbd70e1);light(c,930,300,360,0x998daee5);
   Path ribbon=new Path();ribbon.moveTo(-100,455);ribbon.cubicTo(220,390,530,95,1060,65);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(126);paint.setMaskFilter(new BlurMaskFilter(12,BlurMaskFilter.Blur.NORMAL));paint.setShader(new LinearGradient(0,455,960,65,new int[]{0xe6ffe0aa,0xddd8b6ee,0xeee3e5b3,0xdd9ce1dc},new float[]{0,.4f,.72f,1},Shader.TileMode.CLAMP));c.drawPath(ribbon,paint);paint.setMaskFilter(null);paint.setStrokeWidth(97);c.drawPath(ribbon,paint);paint.setStyle(Paint.Style.FILL);paint.setShader(null);
   Path wave=new Path();wave.moveTo(-180,430);wave.cubicTo(210,140,300,610,1040,80);paint.setShader(new LinearGradient(30,440,960,50,new int[]{0x005e80da,0x14677ac3,0x004074ba},null,Shader.TileMode.CLAMP));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(110);c.drawPath(wave,paint);paint.setStrokeWidth(1.5f);paint.setShader(new LinearGradient(0,540,960,0,new int[]{0x007d93d8,0x166d83c6,0x007d93d8},null,Shader.TileMode.CLAMP));c.drawPath(wave,paint);paint.setStyle(Paint.Style.FILL);paint.setShader(null);
  }
  void light(Canvas c,float x,float y,float r,int tint){paint.setShader(new RadialGradient(x,y,r,tint,Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,paint);}
  protected void onDraw(Canvas c){paint.setShader(null);if(backdrop!=null)c.drawBitmap(backdrop,null,target,paint);paint.setShader(accent);c.drawRect(0,0,width,height,paint);paint.setShader(null);}
  protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(backdrop!=null){backdrop.recycle();backdrop=null;}}
 }
}
