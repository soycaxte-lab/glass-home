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
 private static final String[] COLUMNS={"_id","package_name","title","intent_uri","watch_next_type","last_playback_position_millis","duration_millis","poster_art_uri","last_engagement_time_utc_millis","browsable"};
 private final MainActivity home;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private final ExecutorService images=Executors.newFixedThreadPool(2);
 private final LruCache<String,Bitmap> cache=new LruCache<String,Bitmap>(4*1024*1024){protected int sizeOf(String key,Bitmap b){return b.getAllocationByteCount();}};
 private final ArrayList<Entry> entries=new ArrayList<>();
 private final ArrayList<View> cards=new ArrayList<>();
 private final LinearLayout row;
 private final HorizontalScrollView scroller;
 private final TextView message;
 private boolean active=false,registered=false;
 private int generation=0;
 private String returnKey=null;
 private final Runnable reload=this::load;
 private final ContentObserver observer=new ContentObserver(handler){public void onChange(boolean self){handler.removeCallbacks(reload);handler.postDelayed(reload,350);}};
 private static final class Entry {String key,pkg,title,art;Intent intent;long position,duration,time;}
 WatchNextRow(MainActivity h){
  super(h);home=h;setOrientation(VERTICAL);setPadding(0,h.dp(10),0,h.dp(8));setClipChildren(false);
  TextView heading=h.text("Continue Watching",15,0xffedf3ff);addView(heading,new LayoutParams(-1,h.dp(24)));
  scroller=new HorizontalScrollView(h);scroller.setHorizontalScrollBarEnabled(false);scroller.setClipToPadding(false);scroller.setClipChildren(false);scroller.setFocusable(false);scroller.getViewTreeObserver().addOnScrollChangedListener(()->{View f=home.getCurrentFocus();if(isCard(f))home.moveGlass(f);});addView(scroller,new LayoutParams(-1,0,1));
  row=new LinearLayout(h);row.setPadding(h.dp(6),h.dp(5),h.dp(6),h.dp(4));row.setClipChildren(false);scroller.addView(row,new HorizontalScrollView.LayoutParams(-2,-1));
  message=h.text("Loading your shows…",11,0xff9898a7);row.addView(message);
 }
 void start(){active=true;if(!registered){home.getContentResolver().registerContentObserver(URI,true,observer);registered=true;}load();}
 void stop(){active=false;generation++;handler.removeCallbacks(reload);if(registered){home.getContentResolver().unregisterContentObserver(observer);registered=false;}}
 void destroy(){stop();worker.shutdownNow();images.shutdownNow();cache.evictAll();}
 boolean restorePreferredFocus(){if(returnKey==null)return false;for(int i=0;i<entries.size();i++)if(entries.get(i).key.equals(returnKey)){cards.get(i).post(()->{for(int j=0;j<entries.size();j++)if(entries.get(j).key.equals(returnKey))cards.get(j).requestFocus();});return true;}return false;}
 void clearReturnFocus(){returnKey=null;}
 boolean isCard(View v){return cards.contains(v);}
 boolean focusCard(int index){if(cards.isEmpty())return false;cards.get(Math.min(index,cards.size()-1)).requestFocus();return true;}
 private void load(){
  if(!active)return;final int gen=++generation;
  worker.execute(()->{ArrayList<Entry> result=new ArrayList<>();String error=null;
   if(home.checkSelfPermission("android.permission.READ_TV_LISTINGS")!=PackageManager.PERMISSION_GRANTED)error="Allow TV listings access to show Continue Watching.";
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
     Entry e=new Entry();e.key=pkg+":"+c.getLong(0);e.pkg=pkg;e.title=c.getString(2);e.intent=intent;e.position=c.getLong(5);e.duration=c.getLong(6);e.art=c.getString(7);e.time=c.getLong(8);
     if(e.title==null||e.title.trim().isEmpty())continue;result.add(e);
    }
   }catch(Exception e){android.util.Log.w("GlassWatchNext","Cannot read shared titles",e);error="Continue Watching is unavailable right now.";}
   result.sort((a,b)->Long.compare(b.time,a.time));if(result.size()>12)result.subList(12,result.size()).clear();
   final String status=error;
   handler.post(()->{if(!active||gen!=generation)return;render(result,status,gen);});
  });
 }
 private void render(ArrayList<Entry> items,String error,int gen){
  View focused=home.getCurrentFocus();String key=returnKey;int old=cards.indexOf(focused);if(old>=0&&old<entries.size())key=entries.get(old).key;
  final String focusKey=key;entries.clear();entries.addAll(items);cards.clear();row.removeAllViews();
  if(items.isEmpty()){message.setText(error!=null?error:"No shared titles yet. Start watching in a supported app.");row.addView(message);return;}
  for(int i=0;i<items.size();i++){
   Entry e=items.get(i);final int index=i;
   LinearLayout card=new LinearLayout(home);card.setOrientation(VERTICAL);card.setPadding(home.dp(8),home.dp(7),home.dp(8),home.dp(7));card.setBackground(home.surface(17,false,0x243c4863));card.setFocusable(true);card.setClickable(true);card.setClipChildren(false);
   LayoutParams cp=new LayoutParams(home.dp(200),-1);cp.rightMargin=home.dp(14);row.addView(card,cp);cards.add(card);
   ImageView image=new ImageView(home);image.setScaleType(ImageView.ScaleType.FIT_CENTER);image.setBackground(home.shape(0x88303a53,11));image.setClipToOutline(true);card.addView(image,new LayoutParams(-1,0,1));
   try{Drawable banner=home.getPackageManager().getApplicationBanner(e.pkg);if(banner==null)banner=home.getPackageManager().getApplicationIcon(e.pkg);image.setImageDrawable(banner);}catch(Exception ignored){}
   ProgressBar progress=new ProgressBar(home,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(1000);progress.setProgress(e.duration>0?(int)Math.min(1000,e.position*1000/e.duration):0);progress.setProgressTintList(android.content.res.ColorStateList.valueOf(0xffd9eaff));LayoutParams pp=new LayoutParams(-1,home.dp(2));pp.topMargin=home.dp(5);card.addView(progress,pp);if(e.duration<=0)progress.setVisibility(INVISIBLE);
   TextView label=home.text(e.title,11,0xfff0f4ff);label.setSingleLine(true);label.setEllipsize(android.text.TextUtils.TruncateAt.END);LayoutParams lp=new LayoutParams(-1,home.dp(17));lp.topMargin=home.dp(3);card.addView(label,lp);
   String appName=e.pkg.equals("com.netflix.ninja")?"Netflix":e.pkg.equals("com.amazon.amazonvideo.livingroom")?"Prime Video":e.pkg.equals("com.teveo.tv.v04")?"TEVEO":e.pkg;
   TextView source=home.text(appName,9,0xffb0bed8);card.addView(source,new LayoutParams(-1,home.dp(13)));
   card.setContentDescription(e.title+", "+appName+(e.duration>0?", "+(e.position*100/e.duration)+" percent watched":""));
   card.setOnClickListener(v->{returnKey=e.key;home.open(new Intent(e.intent));});
   card.setOnFocusChangeListener((v,f)->{v.animate().scaleX(f?1.025f:1).scaleY(f?1.025f:1).setDuration(180).start();if(f){home.scroll.smoothScrollTo(0,0);handler.post(()->{home.moveGlass(v);scroller.smoothScrollTo(Math.max(0,v.getLeft()-home.dp(8)),0);});}});
   card.setOnKeyListener((v,k,event)->{if(event.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==KeyEvent.KEYCODE_DPAD_UP){returnKey=null;home.football.focusTab();return true;}if(k==KeyEvent.KEYCODE_DPAD_DOWN){returnKey=null;if(!home.dockTiles.isEmpty())home.dockTiles.get(Math.min(index,home.dockTiles.size()-1)).requestFocus();return true;}if(k==KeyEvent.KEYCODE_DPAD_LEFT||k==KeyEvent.KEYCODE_DPAD_RIGHT){int n=index+(k==KeyEvent.KEYCODE_DPAD_LEFT?-1:1);if(n>=0&&n<cards.size())cards.get(n).requestFocus();else if(n>=cards.size())home.football.focusTab();return true;}return false;});
   if(e.art!=null){Bitmap bitmap=cache.get(e.art);if(bitmap!=null)image.setImageBitmap(bitmap);else images.execute(()->{Bitmap b=artwork(e.art);if(b==null)return;cache.put(e.art,b);handler.post(()->{if(active&&generation==gen)image.setImageBitmap(b);});});}
   if(e.key.equals(focusKey))card.post(()->{if(active&&generation==gen)card.requestFocus();});
  }
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
  try(InputStream in=stream(uri)){if(in==null)return null;ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] bytes=new byte[8192];int n;while((n=in.read(bytes))!=-1){if(data.size()+n>2*1024*1024)return null;data.write(bytes,0,n);}byte[] body=data.toByteArray();BitmapFactory.Options options=new BitmapFactory.Options();options.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(body,0,body.length,options);options.inSampleSize=1;while(options.outWidth/options.inSampleSize>640||options.outHeight/options.inSampleSize>360)options.inSampleSize*=2;options.inJustDecodeBounds=false;options.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeByteArray(body,0,body.length,options);}catch(Exception ignored){return null;}
 }
}

