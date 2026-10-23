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
 FrameLayout root; LinearLayout page,top; RecyclerView grid; ScrollView scroll; TextView title,subtitle,wifi; Glow glow; View glass; WatchNextRow watch; FootballRow football;
 int columns,lastDock=0; String lastPackage=""; boolean alive=true; int generation=0;
 Typeface font(int weight){Typeface base=getResources().getFont(R.font.outfit);return Build.VERSION.SDK_INT>=28?Typeface.create(base,weight,false):base;}
 int dp(float v){return Math.round(v*getResources().getDisplayMetrics().density);}
 TextView text(String s,float size,int color){TextView t=new TextView(this);t.setText(s);t.setTypeface(font(450));t.setFontVariationSettings("'wght' 450");t.setTextSize(size);t.setTextColor(color);t.setIncludeFontPadding(false);return t;}
 GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
 LiquidSurface surface(int radius,boolean focus,int tint){return new LiquidSurface(getResources().getDisplayMetrics().density,radius,focus,tint);}
 @Override public void onCreate(Bundle b){super.onCreate(b);columns=Math.max(4,Math.min(7,getResources().getInteger(R.integer.columns)));lastPackage=getPreferences(0).getString("last","");window();build();}
 void window(){getWindow().getDecorView().setSystemUiVisibility(5894);getWindow().setStatusBarColor(Color.BLACK);getWindow().setNavigationBarColor(Color.BLACK);}
 @Override public void onResume(){super.onResume();window();load();watch.start();football.start();}
 @Override public void onPause(){watch.stop();football.stop();super.onPause();}
 @Override public void onDestroy(){alive=false;generation++;loader.shutdownNow();watch.destroy();football.destroy();super.onDestroy();}
 @Override public void onBackPressed(){watch.clearReturnFocus();if(dockTiles.isEmpty())return;if(scroll.getScrollY()>0){scroll.smoothScrollTo(0,0);dockTiles.get(lastDock).requestFocus();}else dockTiles.get(lastDock).requestFocus();}
 void build(){
 root=new FrameLayout(this);root.setBackgroundColor(getColor(R.color.wallpaper));setContentView(root);
 glow=new Glow();root.addView(glow,new FrameLayout.LayoutParams(-1,-1));
 scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipChildren(false);scroll.setClipToPadding(false);scroll.setVerticalScrollBarEnabled(false);root.addView(scroll,new FrameLayout.LayoutParams(-1,-1));
 page=new LinearLayout(this);page.setOrientation(LinearLayout.VERTICAL);page.setPadding(dp(48),dp(27),dp(48),dp(32));page.setClipChildren(false);page.setClipToPadding(false);scroll.addView(page);
 int screenH=(int)(getResources().getDisplayMetrics().heightPixels/getResources().getDisplayMetrics().density);
 LinearLayout hero=new LinearLayout(this);hero.setOrientation(LinearLayout.VERTICAL);hero.setClipChildren(false);page.addView(hero,new LinearLayout.LayoutParams(-1,dp(Math.max(400,screenH-51))));
 FrameLayout header=new FrameLayout(this);hero.addView(header,new LinearLayout.LayoutParams(-1,dp(104)));
 LinearLayout clockBox=new LinearLayout(this);clockBox.setOrientation(LinearLayout.VERTICAL);header.addView(clockBox,new FrameLayout.LayoutParams(dp(200),-2,Gravity.LEFT|Gravity.CENTER_VERTICAL));
 int h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);clockBox.addView(text(h<12?"Good morning":h<18?"Good afternoon":"Good evening",12,0xffbfc9df));
 TextClock clock=new TextClock(this);clock.setFormat12Hour("h:mm");clock.setFormat24Hour("HH:mm");clock.setTextSize(46);clock.setTypeface(font(200));clock.setFontVariationSettings("'wght' 230");clock.setTextColor(Color.WHITE);clockBox.addView(clock);
 TextClock date=new TextClock(this);date.setFormat12Hour("EEEE, MMMM d");date.setFormat24Hour("EEEE, MMMM d");date.setTextSize(12);date.setTypeface(font(450));date.setTextColor(0xffb4bfd4);clockBox.addView(date);
 football=new FootballRow(this);header.addView(football,new FrameLayout.LayoutParams(dp(216),dp(104),Gravity.CENTER));
 top=new LinearLayout(this);top.setGravity(Gravity.CENTER);header.addView(top,new FrameLayout.LayoutParams(-2,-2,Gravity.RIGHT|Gravity.CENTER_VERTICAL));
 wifi=pill("⌁  Wi-Fi",()->open(new Intent(Settings.ACTION_WIFI_SETTINGS)));TextView settings=pill("⚙  Settings",()->open(new Intent(Settings.ACTION_SETTINGS)));
 top.addView(wifi);top.addView(settings);
 for(int i=0;i<top.getChildCount();i++){final int idx=i;View v=top.getChildAt(i);v.setOnKeyListener((x,k,e)->{if(e.getAction()!=0)return false;if(k==20){scroll.smoothScrollTo(0,0);if(!watch.focusCard(lastDock)&&!dockTiles.isEmpty())dockTiles.get(lastDock).requestFocus();return true;}if(k==19)return true;if(k==21||k==22){int n=idx+(k==21?-1:1);if(n>=0&&n<top.getChildCount())top.getChildAt(n).requestFocus();else if(n<0)football.focusTab();return true;}return false;});}
 LinearLayout media=new LinearLayout(this);media.setClipChildren(false);LinearLayout.LayoutParams mp=new LinearLayout.LayoutParams(-1,0,1);mp.topMargin=dp(7);mp.bottomMargin=dp(7);hero.addView(media,mp);
 watch=new WatchNextRow(this);media.addView(watch,new LinearLayout.LayoutParams(0,-1,1));

 subtitle=text("YOUR APPS",10,0xffa9b9d5);subtitle.setLetterSpacing(.2f);hero.addView(subtitle);
 title=text("Welcome home",27,Color.WHITE);title.setTypeface(font(350));title.setFontVariationSettings("'wght' 350");LinearLayout.LayoutParams tl=new LinearLayout.LayoutParams(-1,-2);tl.topMargin=dp(4);tl.bottomMargin=dp(9);hero.addView(title,tl);
 FrameLayout band=new FrameLayout(this);band.setBackground(surface(30,false,0x40374361));band.setClipChildren(false);hero.addView(band,new LinearLayout.LayoutParams(-1,dp(126)));
 glass=new View(this);glass.setBackground(surface(22,true,0x20536e98));glass.setVisibility(View.INVISIBLE);glass.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);root.addView(glass,new FrameLayout.LayoutParams(1,1)); scroll.getViewTreeObserver().addOnScrollChangedListener(()->{View f=getCurrentFocus();if(f instanceof Tile || watch.isCard(f))moveGlass(f);});
 LinearLayout dockRow=new LinearLayout(this);dockRow.setTag("dock");dockRow.setPadding(dp(18),dp(12),dp(18),dp(12));dockRow.setClipChildren(false);band.addView(dockRow,new FrameLayout.LayoutParams(-1,-1));
 TextView hint=text("All apps   ↓",10,0xff8b9bb7);hint.setGravity(Gravity.CENTER);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(30));hero.addView(hint,hp);
 TextView appsLabel=text("Applications",22,Color.WHITE);appsLabel.setPadding(0,dp(24),0,dp(18));page.addView(appsLabel);
 grid=new RecyclerView(this);grid.setNestedScrollingEnabled(false);grid.setClipChildren(false);grid.setClipToPadding(false);grid.setLayoutManager(new GridLayoutManager(this,columns));grid.setItemAnimator(null);page.addView(grid,new LinearLayout.LayoutParams(-1,-2));
 }
 TextView pill(String label,Runnable action){TextView t=text(label,12,0xffe5e5ed);t.setGravity(Gravity.CENTER);t.setPadding(dp(16),dp(10),dp(16),dp(10));t.setBackground(surface(24,false,0x354b5670));t.setFocusable(true);t.setClickable(true);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(40));lp.leftMargin=dp(8);t.setLayoutParams(lp);t.setOnClickListener(v->action.run());t.setOnFocusChangeListener((v,f)->{v.setBackground(surface(24,f,0x354b5670));t.setTextColor(f?Color.WHITE:0xffe5edff);v.animate().scaleX(f?1.04f:1).scaleY(f?1.04f:1).setDuration(180).start();if(f)glass.setVisibility(View.INVISIBLE);});return t;}
 void open(Intent i){try{startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));}catch(Exception e){Toast.makeText(this,"This option is unavailable on this box",Toast.LENGTH_SHORT).show();}}
 void load(){final int gen=++generation;loader.execute(()->{ArrayList<App> found=new ArrayList<>();HashSet<String> seen=new HashSet<>();PackageManager pm=getPackageManager();
 for(String category:new String[]{Intent.CATEGORY_LEANBACK_LAUNCHER,Intent.CATEGORY_LAUNCHER}){Intent q=new Intent(Intent.ACTION_MAIN).addCategory(category);for(ResolveInfo ri:pm.queryIntentActivities(q,0)){String p=ri.activityInfo.packageName;if(p.equals(getPackageName())||!seen.add(p))continue;Intent launch=new Intent(Intent.ACTION_MAIN).addCategory(category).setComponent(new ComponentName(p,ri.activityInfo.name));Drawable art=ri.activityInfo.loadBanner(pm);if(art==null)art=ri.loadIcon(pm);String name=ri.loadLabel(pm).toString();if(p.equals("com.teveo.tv.v04"))name="TEVEO";int color=sample(art);App app=new App(name,p,launch,art,color);app.icon=ri.loadIcon(pm);found.add(app);}}
 found.sort((a,b)->a.name.compareToIgnoreCase(b.name));runOnUiThread(()->{if(!alive||gen!=generation)return;all.clear();all.addAll(found);render();});});
 String network="Wi-Fi";try{WifiManager wm=(WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);String ssid=wm.getConnectionInfo().getSSID();if(ssid!=null&&!ssid.contains("unknown"))network=ssid.replace("\"","");}catch(Exception ignored){}wifi.setText("⌁  "+network);
 }
 int sample(Drawable d){try{Bitmap b=Bitmap.createBitmap(24,14,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);d.setBounds(0,0,24,14);d.draw(c);int r=0,g=0,bl=0,n=0;for(int y=0;y<14;y++)for(int x=0;x<24;x++){int v=b.getPixel(x,y);if(Math.max(Color.red(v),Math.max(Color.green(v),Color.blue(v)))-Math.min(Color.red(v),Math.min(Color.green(v),Color.blue(v)))>45){r+=Color.red(v);g+=Color.green(v);bl+=Color.blue(v);n++;}}b.recycle();return n==0?0xff384080:Color.rgb(r/n,g/n,bl/n);}catch(Exception e){return 0xff384080;}}
 void render(){dock.clear();others.clear();dockTiles.clear();for(String p:getResources().getStringArray(R.array.dock_apps))for(App a:all)if(a.pkg.equals(p)&&dock.size()<columns)dock.add(a);for(App a:all)if(!dock.contains(a))others.add(a);
 LinearLayout row=page.findViewWithTag("dock");row.removeAllViews();for(int i=0;i<dock.size();i++){Tile t=new Tile(dock.get(i),i,true);dockTiles.add(t);row.addView(t,new LinearLayout.LayoutParams(0,-1,1));}grid.setAdapter(new Adapter());
 if(watch.restorePreferredFocus())return; for(int i=0;i<others.size();i++){if(others.get(i).pkg.equals(lastPackage)){final int pos=i;grid.post(()->focusGrid(pos));return;}} View target=null;for(Tile t:dockTiles)if(t.app.pkg.equals(lastPackage))target=t;if(target==null&&!dockTiles.isEmpty())target=dockTiles.get(Math.min(lastDock,dockTiles.size()-1));if(target!=null){View finalTarget=target;target.post(()->finalTarget.requestFocus());}}
 class Tile extends LinearLayout {App app;int index;boolean isDock;ImageView image;TextView label;
 Tile(App a,int i,boolean d){super(MainActivity.this);app=a;index=i;isDock=d;setOrientation(VERTICAL);setGravity(Gravity.CENTER);setPadding(dp(10),dp(5),dp(10),dp(5));setFocusable(true);setClickable(true);setClipChildren(false);
 image=new ImageView(MainActivity.this);image.setImageDrawable(d?a.icon:a.art);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setBackground(surface(d?19:14,false,0x183e4e6e));image.setClipToOutline(true);image.setPadding(dp(d?3:8),dp(d?3:6),dp(d?3:8),dp(d?3:6));addView(image,new LinearLayout.LayoutParams(d?dp(70):-1,dp(d?70:77)));
 label=text(a.name,d?11:12,0xffd6d6de);label.setGravity(Gravity.CENTER);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(18));lp.topMargin=dp(5);addView(label,lp);
 setOnClickListener(v->{lastPackage=app.pkg;open(app.intent);});setOnFocusChangeListener((v,f)->{animate().scaleX(f?(isDock?1.09f:1.035f):1).scaleY(f?(isDock?1.09f:1.035f):1).translationY(f&&isDock?-dp(2):0).setDuration(240).setInterpolator(new OvershootInterpolator(.8f)).start();label.setTextColor(f?Color.WHITE:0xffbec9df);if(f){lastPackage=app.pkg;getPreferences(0).edit().putString("last",lastPackage).apply();if(isDock){lastDock=index;title.setText(app.name);glow.setColor(app.color);scroll.smoothScrollTo(0,0);post(()->moveGlass(this));}else{post(()->moveGlass(this));scroll.post(()->{int[] pos=new int[2];getLocationOnScreen(pos);if(pos[1]+getHeight()>root.getHeight()-dp(25))scroll.smoothScrollBy(0,pos[1]+getHeight()-root.getHeight()+dp(35));});}}});
 setOnKeyListener((v,k,e)->{if(e.getAction()!=0)return false;if(k!=19&&k!=20&&k!=21&&k!=22)return false;
 if(isDock){if(k==19){if(!watch.focusCard(index))football.focusTab();return true;}if(k==20){focusGrid(Math.min(index,others.size()-1));return true;}int next=index+(k==21?-1:1);if(next>=0&&next<dockTiles.size())dockTiles.get(next).requestFocus();return true;}
 int next=index;if(k==19){if(index<columns){dockTiles.get(Math.min(index,dockTiles.size()-1)).requestFocus();return true;}next-=columns;}if(k==20)next+=columns;if(k==21&&index%columns>0)next--;if(k==22&&index%columns<columns-1)next++;if(next>=0&&next<others.size())focusGrid(next);return true;});
 }
 }
 void moveGlass(View tile){FrameLayout band=(FrameLayout)glass.getParent();int[] a=new int[2],b=new int[2];tile.getLocationInWindow(a);band.getLocationInWindow(b);FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams)glass.getLayoutParams();if(lp.width!=tile.getWidth()||lp.height!=tile.getHeight()){lp.width=tile.getWidth();lp.height=tile.getHeight();glass.setLayoutParams(lp);}float x=a[0]-b[0],y=a[1]-b[1];if(glass.getVisibility()!=View.VISIBLE){glass.setTranslationX(x);glass.setTranslationY(y);glass.setVisibility(View.VISIBLE);}else glass.animate().translationX(x).translationY(y).setDuration(240).setInterpolator(new OvershootInterpolator(.7f)).start();}
 void focusGrid(int n){if(n<0||n>=others.size())return;RecyclerView.ViewHolder h=grid.findViewHolderForAdapterPosition(n);if(h!=null)h.itemView.requestFocus();else{grid.scrollToPosition(n);grid.post(()->{RecyclerView.ViewHolder vh=grid.findViewHolderForAdapterPosition(n);if(vh!=null)vh.itemView.requestFocus();});}}
 class Holder extends RecyclerView.ViewHolder{Holder(View v){super(v);}}
 class Adapter extends RecyclerView.Adapter<Holder>{@Override public int getItemCount(){return others.size();}@Override public int getItemViewType(int pos){return pos;}@Override public Holder onCreateViewHolder(ViewGroup parent,int pos){Tile t=new Tile(others.get(pos),pos,false);t.setLayoutParams(new RecyclerView.LayoutParams(-1,dp(125)));return new Holder(t);}@Override public void onBindViewHolder(Holder h,int pos){} }
 class Glow extends View {
  final Paint paint=new Paint(3);final Rect target=new Rect();Shader accent;int color=0xff6e67b8;Bitmap backdrop;int width,height;
  Glow(){super(MainActivity.this);paint.setDither(true);}
  void setColor(int value){color=value;accentShader();invalidate();} void accentShader(){if(width>0)accent=new RadialGradient(width*.33f,height*.77f,width*.42f,(color&0xffffff)|0x18000000,Color.TRANSPARENT,Shader.TileMode.CLAMP);}
  protected void onSizeChanged(int w,int h,int ow,int oh){width=w;height=h;target.set(0,0,w,h);accentShader();if(backdrop!=null)backdrop.recycle();backdrop=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(backdrop);
   paint.setShader(new LinearGradient(0,0,960,540,new int[]{0xff050915,0xff131a36,0xff080c1d},new float[]{0,.52f,1},Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,paint);
   light(c,110,420,610,0x9a48389b);light(c,835,240,530,0x9a174f76);light(c,650,555,480,0x8a314c9c);light(c,170,-100,520,0x66252e5b);
   Path wave=new Path();wave.moveTo(-180,430);wave.cubicTo(210,140,300,610,1040,80);paint.setShader(new LinearGradient(30,440,960,50,new int[]{0x005e80da,0x14677ac3,0x004074ba},null,Shader.TileMode.CLAMP));paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(110);c.drawPath(wave,paint);paint.setStrokeWidth(1.5f);paint.setShader(new LinearGradient(0,540,960,0,new int[]{0x007d93d8,0x166d83c6,0x007d93d8},null,Shader.TileMode.CLAMP));c.drawPath(wave,paint);paint.setStyle(Paint.Style.FILL);paint.setShader(null);
  }
  void light(Canvas c,float x,float y,float r,int tint){paint.setShader(new RadialGradient(x,y,r,tint,Color.TRANSPARENT,Shader.TileMode.CLAMP));c.drawRect(0,0,960,540,paint);}
  protected void onDraw(Canvas c){paint.setShader(null);if(backdrop!=null)c.drawBitmap(backdrop,null,target,paint);paint.setShader(accent);c.drawRect(0,0,width,height,paint);paint.setShader(null);}
  protected void onDetachedFromWindow(){super.onDetachedFromWindow();if(backdrop!=null){backdrop.recycle();backdrop=null;}}
 }
}
