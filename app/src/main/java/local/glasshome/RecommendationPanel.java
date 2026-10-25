package local.glasshome;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.util.AtomicFile;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.*;

/** Public editorial discovery, separate from the user's private Watch Next history. */
final class RecommendationPanel extends LinearLayout {
 private static final String FEED="https://itunes.apple.com/es/rss/topmovies/limit=25/json";
 private static final long TTL=24*60*60*1000L, RETRY=15*60*1000L;
 private final MainActivity home;
 private final FrameLayout card;
 private final ImageView image;
 private final TextView title,caption,cta;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private final ExecutorService worker=Executors.newSingleThreadExecutor();
 private volatile boolean active;
 private int generation; private boolean loading,reloadRequested; private long memoryLoaded,memoryChecked; private LocalDate memoryDay; private int memoryMode=-1;
 private Movie current; private int mode=0; private final TextView heading;
 private Bitmap bitmap;
 private static final class Movie {String name,summary,genre,year,art,url,rights,source;}

 RecommendationPanel(MainActivity h){
  super(h);home=h;setOrientation(VERTICAL);setClipChildren(false);
  heading=h.text("Películas para descubrir",15,0xffffffff);addView(heading,new LayoutParams(-1,h.dp(22)));
  card=new FrameLayout(h);card.setBackground(h.shape(0xff20397a,9));card.setClipToOutline(true);card.setFocusable(true);card.setClickable(true);addView(card,new LayoutParams(-1,0,1));
  image=new ImageView(h);image.setScaleType(ImageView.ScaleType.CENTER_CROP);card.addView(image,new FrameLayout.LayoutParams(-1,-1));
  View shade=new View(h);shade.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0x00081936,0x10081936,0xf5081936}));card.addView(shade,new FrameLayout.LayoutParams(-1,-1));
  LinearLayout details=new LinearLayout(h);details.setOrientation(VERTICAL);details.setPadding(h.dp(12),h.dp(8),h.dp(12),h.dp(10));card.addView(details,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
  title=h.text("Descubre tu próxima película",19,0xffffffff);title.setTypeface(h.font(550));title.setFontVariationSettings("'wght' 550");title.setMaxLines(2);title.setEllipsize(android.text.TextUtils.TruncateAt.END);details.addView(title,new LayoutParams(-1,-2));
  LinearLayout bottom=new LinearLayout(h);bottom.setGravity(Gravity.CENTER_VERTICAL);details.addView(bottom,new LayoutParams(-1,h.dp(30)));
  caption=h.text("Cargando catálogo…",10,0xffe2ebff);caption.setSingleLine(true);caption.setEllipsize(android.text.TextUtils.TruncateAt.END);bottom.addView(caption,new LayoutParams(0,-2,1));
  cta=h.text("▶ Ver detalles",11,0xffffffff);cta.setPadding(h.dp(12),h.dp(5),h.dp(12),h.dp(5));cta.setBackground(h.surface(16,false,0x605b709d));bottom.addView(cta,new LayoutParams(-2,-2));
  card.setOnClickListener(v->details());card.setOnFocusChangeListener((v,f)->{v.animate().scaleX(f?1.018f:1).scaleY(f?1.018f:1).setDuration(180).start();if(f){h.scroll.smoothScrollTo(0,0);h.moveGlass(v);}});
  card.setOnKeyListener((v,k,e)->{if(e.getAction()!=KeyEvent.ACTION_DOWN)return false;if(k==19){h.focusCategory();return true;}if(k==20){if(!h.dockTiles.isEmpty())h.dockTiles.get(0).requestFocus();return true;}if(k==22){h.watch.focusCard(0);return true;}return k==21;});
 }
 boolean focusCard(){card.requestFocus();return true;}
 void setFilter(int filter){int next=filter==2?2:0;if(next==mode)return;mode=next;current=null;image.setImageDrawable(null);heading.setText(mode==2?"Series para descubrir":"Películas para descubrir");title.setText("Descubre tu próxima "+(mode==2?"serie":"película"));caption.setText("Cargando catálogo…");if(active)start();}
 void start(){
  active=true;
  if(current!=null&&bitmap!=null&&memoryMode==mode&&LocalDate.now().equals(memoryDay)&&(SystemClock.elapsedRealtime()-memoryLoaded<TTL||SystemClock.elapsedRealtime()-memoryChecked<60000))return;
  if(loading){reloadRequested=true;return;}
  loading=true;reloadRequested=false;memoryChecked=SystemClock.elapsedRealtime();final int gen=++generation;final int selectedMode=mode;
  worker.execute(()->{
   try{
    File file=new File(home.getFilesDir(),"discovery-apple-"+(selectedMode==2?"series-us":"movies-es")+".json");
    String body=null;Movie cached=null;Bitmap cachedArt=null;
    if(file.isFile())try{body=new String(readFile(file,2*1024*1024),StandardCharsets.UTF_8);cached=parse(body,selectedMode);cachedArt=loadArt(cached.art,selectedMode);publish(cached,cachedArt,selectedMode,gen);}catch(Exception ignored){body=null;}
    long now=System.currentTimeMillis(),last=home.getPreferences(0).getLong("discovery-attempt-"+selectedMode,0);
    if((body==null||now-file.lastModified()>TTL)&&now-last>RETRY){
     home.getPreferences(0).edit().putLong("discovery-attempt-"+selectedMode,now).apply();
     try{
      String fresh=new String(download(selectedMode==2?"https://itunes.apple.com/us/rss/toptvseasons/limit=25/json":FEED,2*1024*1024),StandardCharsets.UTF_8);
      Movie movie=parse(fresh,selectedMode);Bitmap art=cached!=null&&movie.art.equals(cached.art)?cachedArt:loadArt(movie.art,selectedMode);
      write(file,fresh.getBytes(StandardCharsets.UTF_8));publish(movie,art,selectedMode,gen);body=fresh;
     }catch(Exception ignored){}
    }
    if(body==null)throw new IOException("Catalog unavailable");
   }catch(Exception ignored){handler.post(()->{if(active&&generation==gen&&current==null){caption.setText("Catálogo no disponible; vuelve a intentarlo más tarde");cta.setVisibility(GONE);}});}
   finally{handler.post(()->{loading=false;if(active&&(reloadRequested||generation!=gen))start();});}
  });
 }
 private void publish(Movie movie,Bitmap art,int selectedMode,int gen){handler.post(()->{
  if(!active||generation!=gen)return;
  current=movie;if(bitmap!=art){image.setImageBitmap(art);bitmap=art;}
  title.setText(movie.name);caption.setText(movie.genre+(movie.year.isEmpty()?"":" · "+movie.year));cta.setVisibility(VISIBLE);card.setContentDescription("Descubrir "+movie.name+", ver detalles del catálogo");
  memoryMode=selectedMode;memoryDay=LocalDate.now();
  File file=new File(home.getFilesDir(),"discovery-apple-"+(selectedMode==2?"series-us":"movies-es")+".json");
  long age=Math.max(0,System.currentTimeMillis()-file.lastModified());
  // Do not give an expired disk cache another full day in memory.
  memoryLoaded=SystemClock.elapsedRealtime()-Math.min(TTL,age);
 });}
 void stop(){active=false;generation++;}
 void destroy(){stop();worker.shutdownNow();handler.removeCallbacksAndMessages(null);image.setImageDrawable(null);bitmap=null;}
 private void details(){if(current==null){Toast.makeText(home,"El catálogo todavía no está disponible",Toast.LENGTH_SHORT).show();return;}Movie m=current;
  String message=m.genre+(m.year.isEmpty()?"":" · "+m.year)+"\n\n"+m.summary+"\n\nFuente: "+m.source+". No es una recomendación personalizada. La disponibilidad, el alquiler y la compra dependen del país y del proveedor; no indica disponibilidad en Netflix, TEVEO o Prime Video.";
  if(!m.rights.isEmpty())message+="\n\n"+m.rights;
  new AlertDialog.Builder(home).setTitle(m.name).setMessage(message).setPositiveButton("Ver en el catálogo",(d,w)->{if(allowed(m.url))home.open(new Intent(Intent.ACTION_VIEW,Uri.parse(m.url)));}).setNegativeButton("Cerrar",null).show();
 }
 private static String label(JSONObject object,String key){return object.optJSONObject(key)==null?"":object.optJSONObject(key).optString("label","");}
 private static Movie parse(String body,int mode)throws JSONException{
  JSONArray entries=new JSONObject(body).getJSONObject("feed").getJSONArray("entry");if(entries.length()==0)throw new JSONException("Empty catalog");
  int start=(int)Math.floorMod(LocalDate.now().toEpochDay(),entries.length());
  for(int i=0;i<entries.length();i++){
   JSONObject e=entries.getJSONObject((start+i)%entries.length());Movie m=new Movie();m.source=mode==2?"series populares del catálogo Apple de EE. UU.":"películas populares del catálogo Apple de España";m.name=label(e,"im:name");m.summary=label(e,"summary");m.rights=label(e,"rights");m.year=label(e,"im:releaseDate");if(m.year.length()>=4)m.year=m.year.substring(0,4);
   JSONObject cat=e.optJSONObject("category");m.genre=cat==null?"Película":cat.optJSONObject("attributes")==null?"Película":cat.getJSONObject("attributes").optString("label","Película");
   JSONArray pics=e.optJSONArray("im:image");if(pics==null||pics.length()==0)continue;m.art=pics.getJSONObject(pics.length()-1).optString("label","");
   // Request a larger rendition of the same publicly supplied artwork; no new title art is invented.
   m.art=m.art.replaceAll("/[0-9]+x[0-9]+bb\\.(jpg|png)$","/1000x750bb.$1");
   Object link=e.opt("link");JSONObject value=link instanceof JSONArray?((JSONArray)link).optJSONObject(0):link instanceof JSONObject?(JSONObject)link:null;
   m.url=value==null||value.optJSONObject("attributes")==null?"":value.getJSONObject("attributes").optString("href","");
   if(!m.name.isEmpty()&&allowed(m.art)&&allowed(m.url))return m;
  }throw new JSONException("No usable catalog item");
 }
 private Bitmap loadArt(String uri,int mode){try{
  File picture=new File(home.getFilesDir(),"discovery-image-"+mode+".bin"),address=new File(home.getFilesDir(),"discovery-image-"+mode+".url");byte[] bytes=null;
  if(picture.isFile()&&address.isFile()&&uri.equals(new String(readFile(address,8192),StandardCharsets.UTF_8)))bytes=readFile(picture,3*1024*1024);
  if(bytes==null){bytes=download(uri,3*1024*1024);write(picture,bytes);write(address,uri.getBytes(StandardCharsets.UTF_8));}
  BitmapFactory.Options o=new BitmapFactory.Options();o.inJustDecodeBounds=true;BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);if(o.outWidth<=0||o.outHeight<=0)return null;o.inSampleSize=1;while(o.outWidth/o.inSampleSize>1280||o.outHeight/o.inSampleSize>800)o.inSampleSize*=2;o.inJustDecodeBounds=false;o.inPreferredConfig=Bitmap.Config.RGB_565;return BitmapFactory.decodeByteArray(bytes,0,bytes.length,o);
 }catch(Exception ignored){return null;}}
 private static boolean allowed(String link){try{URI u=new URI(link);String h=u.getHost();return "https".equals(u.getScheme())&&h!=null&&(h.equals("itunes.apple.com")||h.equals("tv.apple.com")||h.endsWith(".mzstatic.com"));}catch(Exception e){return false;}}
 private static byte[] download(String url,int max)throws IOException{
  for(int hop=0;hop<4;hop++){if(!allowed(url))throw new IOException("Unsupported source");HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setConnectTimeout(5000);c.setReadTimeout(5000);c.setInstanceFollowRedirects(false);
   try{int status=c.getResponseCode();if(status>=300&&status<400){String location=c.getHeaderField("Location");if(location==null)throw new IOException("Missing redirect");url=new URL(new URL(url),location).toString();continue;}if(status!=200)throw new IOException("HTTP "+status);try(InputStream in=c.getInputStream()){return read(in,max);}}finally{c.disconnect();}
  }throw new IOException("Too many redirects");
 }
 private static byte[] readFile(File f,int max)throws IOException{try(InputStream in=new FileInputStream(f)){return read(in,max);}}
 private static byte[] read(InputStream in,int max)throws IOException{ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1){if(out.size()+n>max)throw new IOException("Response too large");out.write(b,0,n);}return out.toByteArray();}
 private static void write(File f,byte[] bytes)throws IOException{AtomicFile file=new AtomicFile(f);FileOutputStream out=null;try{out=file.startWrite();out.write(bytes);file.finishWrite(out);}catch(IOException e){if(out!=null)file.failWrite(out);throw e;}}
}
