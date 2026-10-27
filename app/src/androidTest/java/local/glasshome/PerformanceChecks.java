package local.glasshome;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.lang.reflect.*;
import java.util.*;

/** Device regression checks: actual views, lifecycle and keyed updates; no provider writes. */
public final class PerformanceChecks extends Instrumentation {
 private int checks; private boolean teamsOnly;
 private static void require(boolean value,String message){if(!value)throw new AssertionError(message);}
 private static Object field(Object value,String name)throws Exception{Field f=value.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(value);}
 private static void set(Object object,String name,Object value)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);f.set(object,value);}
 public void onCreate(Bundle args){super.onCreate(args);teamsOnly="teams".equals(args.getString("suite"));start();}
 public void onStart(){if(teamsOnly){Bundle result=new Bundle();try{result.putString("result",TeamSelectionChecks.run(this));finish(Activity.RESULT_OK,result);}catch(Throwable t){result.putString("failure",t.toString());finish(Activity.RESULT_CANCELED,result);}return;}Bundle result=new Bundle();MainActivity home=null;try{
  home=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
  final MainActivity activity=home;
  long deadline=SystemClock.uptimeMillis()+10000;
  while(SystemClock.uptimeMillis()<deadline){final boolean[] ready={false};runOnMainSync(()->ready[0]=activity.appsLoaded&&!activity.dockTiles.isEmpty());if(ready[0])break;SystemClock.sleep(50);}
  runOnMainSync(()->require(activity.appsLoaded&&!activity.dockTiles.isEmpty(),"App catalog did not load"));checks++;
  final View[] originalDock={null};final Object[] originalAdapter={null};final int[] originalGeneration={0};
  runOnMainSync(()->{originalDock[0]=activity.dockTiles.get(0);originalAdapter[0]=activity.grid.getAdapter();originalGeneration[0]=activity.generation;});
  for(int i=0;i<5;i++){runOnMainSync(()->{callActivityOnPause(activity);callActivityOnResume(activity);});waitForIdleSync();}
  SystemClock.sleep(1000);
  runOnMainSync(()->{require(activity.dockTiles.get(0)==originalDock[0],"Dock rebuilt after returning");require(activity.grid.getAdapter()==originalAdapter[0],"Grid adapter replaced");require(activity.generation==originalGeneration[0],"Applications rescanned on return");});checks+=3;
  runOnMainSync(()->{require(activity.appAdapter.getItemViewType(0)==activity.appAdapter.getItemViewType(1),"Grid does not share recycled view type");});checks++;
  final Throwable[] failure={null};
  runOnMainSync(()->{try{
   WatchNextRow watch=activity.watch;watch.stop();watch.clearReturnFocus();
   Class<?> type=Class.forName("local.glasshome.WatchNextRow$Entry");Constructor<?> constructor=type.getDeclaredConstructor();constructor.setAccessible(true);
   Object first=entry(constructor,"check:a","Prueba local A",1000),second=entry(constructor,"check:b","Prueba local B",2000);
   Method render=WatchNextRow.class.getDeclaredMethod("render",ArrayList.class,String.class,int.class);render.setAccessible(true);
   int generation=(Integer)field(watch,"generation");ArrayList<Object> entries=new ArrayList<>(Arrays.asList(first,second));render.invoke(watch,entries,null,generation);
   LinearLayout row=(LinearLayout)field(watch,"row");View firstView=row.getChildAt(0),secondView=row.getChildAt(1);firstView.requestFocus();
   Object revised=entry(constructor,"check:a","Prueba actualizada",5000);entries.set(0,revised);render.invoke(watch,entries,null,generation);
   require(row.getChildAt(0)==firstView&&row.getChildAt(1)==secondView,"Unchanged cards recreated");checks++;
   require(((ProgressBar)field(firstView,"progress")).getProgress()==500,"Playback progress not updated");checks++;
   require(((TextView)field(firstView,"label")).getText().toString().equals("Prueba actualizada"),"Title not updated");checks++;
   require(firstView.hasFocus(),"Focus lost on progress update");checks++;
   Collections.reverse(entries);render.invoke(watch,entries,null,generation);require(row.getChildAt(1)==firstView&&firstView.hasFocus(),"Focus/identity lost when reordered");checks++;
   entries.remove(revised);render.invoke(watch,entries,null,generation);require(row.getChildCount()==1&&row.getChildAt(0)==secondView,"Card removal rebuilt survivor");checks++;
   render.invoke(watch,new ArrayList<>(),null,generation);require(row.getChildCount()==1&&row.getChildAt(0) instanceof TextView,"Empty state broken");checks++;
   activity.focusCategory();watch.start();
  }catch(Throwable t){failure[0]=t;}});
  if(failure[0]!=null)throw new AssertionError(failure[0]);
  result.putString("result","PASS: "+checks+" lifecycle, recycling, progress, focus and empty-state checks");finish(Activity.RESULT_OK,result);
 }catch(Throwable t){result.putString("failure",t.toString());finish(Activity.RESULT_CANCELED,result);}}
 private static Object entry(Constructor<?> constructor,String key,String title,long position)throws Exception{
  Object entry=constructor.newInstance();set(entry,"key",key);set(entry,"pkg","local.glasshome");set(entry,"title",title);set(entry,"position",position);set(entry,"duration",10000L);set(entry,"intent",new Intent(Intent.ACTION_MAIN));return entry;
 }
}
