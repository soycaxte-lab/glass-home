package local.glasshome;

import android.os.Bundle;import android.app.*;import android.content.*;import android.widget.*;import android.view.*;import java.lang.reflect.*;import java.util.*;

/** Actual TV dialog interactions; restores preferences and never modifies shared history. */
final class TeamSelectionChecks {
 private interface Action {void run()throws Exception;}
 private static void ui(Instrumentation i,Action action)throws Exception{Throwable[] error={null};i.runOnMainSync(()->{try{action.run();}catch(Throwable t){error[0]=t;}});if(error[0]!=null)throw new Exception(error[0]);i.waitForIdleSync();}
 private static Object field(Object object,String key)throws Exception{Field f=object.getClass().getDeclaredField(key);f.setAccessible(true);return f.get(object);}
 private static void progress(Instrumentation i,String phase){Bundle state=new Bundle();state.putString("phase",phase);i.sendStatus(0,state);}
 private static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
 private static <T extends View>T view(View root,Class<T> type){if(type.isInstance(root))return type.cast(root);if(root instanceof ViewGroup)for(int n=0;n<((ViewGroup)root).getChildCount();n++){T found=view(((ViewGroup)root).getChildAt(n),type);if(found!=null)return found;}return null;}
 static String run(Instrumentation instrumentation)throws Exception{
  MainActivity[] activity={(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))};
  progress(instrumentation,"started");
  String original=activity[0].getPreferences(0).getString(FootballTeams.PREFERENCE,null);int checks=0;
  try{
   FootballRow widget=activity[0].football;FootballTeams catalog=(FootballTeams)field(widget,"catalog");
   ui(instrumentation,()->{widget.stop();widget.saveTeams(catalog.restore("co,real-madrid"));check(activity[0].getPreferences(0).getString(FootballTeams.PREFERENCE,"").equals("co,real-madrid"),"Selection not saved");});checks++;
   ui(instrumentation,widget::showTeams);AlertDialog[] dialog={(AlertDialog)field(widget,"teamDialog")};
   check(dialog[0]!=null&&dialog[0].isShowing(),"Selector not shown");checks++;
   ui(instrumentation,()->view(dialog[0].getWindow().getDecorView(),EditText.class).setText("bayern"));
   ListView[] options={view(dialog[0].getWindow().getDecorView(),ListView.class)};
   check(options[0].getCount()==1,"Search did not filter teams");checks++;
   ui(instrumentation,()->{options[0].performItemClick(options[0].getChildAt(0),0,0);dialog[0].getButton(DialogInterface.BUTTON_NEGATIVE).performClick();});
   check(activity[0].getPreferences(0).getString(FootballTeams.PREFERENCE,"").equals("co,real-madrid"),"Cancel changed favorites");checks++;
   ui(instrumentation,widget::showTeams);dialog[0]=(AlertDialog)field(widget,"teamDialog");
   ui(instrumentation,()->view(dialog[0].getWindow().getDecorView(),EditText.class).setText("bayern"));options[0]=view(dialog[0].getWindow().getDecorView(),ListView.class);
   ui(instrumentation,()->{options[0].performItemClick(options[0].getChildAt(0),0,0);dialog[0].getButton(DialogInterface.BUTTON_POSITIVE).performClick();});
   check(activity[0].getPreferences(0).getString(FootballTeams.PREFERENCE,"").equals("co,real-madrid,bayern-munchen"),"Filtered selection lost previous favorites");checks++;
   ui(instrumentation,widget::showTeams);dialog[0]=(AlertDialog)field(widget,"teamDialog");
   ui(instrumentation,()->view(dialog[0].getWindow().getDecorView(),EditText.class).setText("bayern"));options[0]=view(dialog[0].getWindow().getDecorView(),ListView.class);
   check(options[0].isItemChecked(0),"Saved favorite missing when reopened");checks++;
   ui(instrumentation,()->dialog[0].getButton(DialogInterface.BUTTON_NEUTRAL).performClick());
   check(activity[0].getPreferences(0).getString(FootballTeams.PREFERENCE,"").endsWith("bayern-munchen"),"Originals saved without confirmation");checks++;
   ui(instrumentation,()->view(dialog[0].getWindow().getDecorView(),EditText.class).setText("liverpool inglaterra"));options[0]=view(dialog[0].getWindow().getDecorView(),ListView.class);
   check(options[0].getCount()==1,"Additional team unavailable; count="+options[0].getCount()+", query="+view(dialog[0].getWindow().getDecorView(),EditText.class).getText());
   progress(instrumentation,"before_limit");ui(instrumentation,()->options[0].performItemClick(options[0].getChildAt(0),0,0));progress(instrumentation,"after_limit");check(!options[0].isItemChecked(0),"Sixth team accepted");checks++;
   ui(instrumentation,()->dialog[0].getButton(DialogInterface.BUTTON_POSITIVE).performClick());check(activity[0].getPreferences(0).getString(FootballTeams.PREFERENCE,"").equals(catalog.serialize(catalog.defaults())),"Restore originals failed");checks++;
   ui(instrumentation,widget::showTeams);dialog[0]=(AlertDialog)field(widget,"teamDialog");options[0]=view(dialog[0].getWindow().getDecorView(),ListView.class);
   progress(instrumentation,"before_empty");ui(instrumentation,()->{for(int n=0;n<options[0].getCount();n++)if(options[0].isItemChecked(n))options[0].performItemClick(null,n,n);dialog[0].getButton(DialogInterface.BUTTON_POSITIVE).performClick();});progress(instrumentation,"after_empty");check(dialog[0].isShowing(),"Empty selection dismissed dialog");checks++;
   ui(instrumentation,()->{dialog[0].getButton(DialogInterface.BUTTON_NEGATIVE).performClick();widget.saveTeams(catalog.restore("co,real-madrid"));activity[0].finish();});
   progress(instrumentation,"before_recreation");MainActivity next=(MainActivity)instrumentation.startActivitySync(new Intent(instrumentation.getTargetContext(),MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));activity[0]=next;
   ui(instrumentation,()->{next.football.stop();List<?> restored=(List<?>)field(next.football,"teams");check(restored.size()==2&&((FootballTeams.Team)restored.get(0)).id.equals("co"),"Favorites lost after activity recreation");});checks++;
   progress(instrumentation,"complete");return "PASS: "+checks+" TV dialog, filtering, cancel/save, limits, reset and recreation checks";
  }finally{ui(instrumentation,()->{FootballTeams catalog=(FootballTeams)field(activity[0].football,"catalog");activity[0].football.saveTeams(catalog.restore(original));if(original==null)activity[0].getPreferences(0).edit().remove(FootballTeams.PREFERENCE).apply();else activity[0].getPreferences(0).edit().putString(FootballTeams.PREFERENCE,original).apply();});}
 }
}
