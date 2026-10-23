package local.glasshome;

import java.time.*;
import java.nio.file.*;
import java.util.*;

/** Run with javac/java; exercises local-day boundaries, folding, deduplication and feed failures. */
public final class FootballFixturesCheck {
 static int checks=0;
 static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 static String feed(String events){return "BEGIN:VCALENDAR\r\n"+events+"END:VCALENDAR\r\n";}
 static String event(String summary,String start,String extra){return "BEGIN:VEVENT\r\nSUMMARY:"+summary+"\r\nDTSTART"+start+"\r\n"+extra+"END:VEVENT\r\n";}
 public static void main(String[] args)throws Exception{
  ZoneId aruba=ZoneId.of("America/Aruba");LocalDate day=LocalDate.of(2026,10,22);
  List<FootballFixtures.Match> a=FootballFixtures.parse(feed(event("Colombia - Chile [Friendlies]",":20261023T000000Z","")),4);
  check(a.size()==1,"UTC feed");check(a.get(0).day(aruba).equals(day),"UTC midnight belongs to previous day in Aruba");
  check(FootballFixtures.forDay(a,day.plusDays(1),aruba).isEmpty(),"Tomorrow does not duplicate today's local match");
  check(a.get(0).competition.equals("Friendlies"),"Competition separated");
  List<FootballFixtures.Match> b=FootballFixtures.parse(feed(event("Real Madrid - Barcelona (2-1)",";TZID=Europe/Madrid:20261022T210000","")),0);
  check(b.get(0).kickoff.equals(Instant.parse("2026-10-22T19:00:00Z")),"IANA timezone conversion");
  check(b.get(0).title.equals("Real Madrid - Barcelona"),"Historical score stripped");
  List<FootballFixtures.Match> both=new ArrayList<>(b);both.addAll(FootballFixtures.parse(feed(event("Real Madrid - Barcelona",":20261022T190000Z","")),2));
  check(FootballFixtures.forDay(both,day,aruba).size()==1,"Match between favorites deduplicated");
  check(FootballFixtures.parse(feed(event("Cancelled",":20261022T190000Z","STATUS:CANCELLED\r\n")),0).isEmpty(),"Cancelled event excluded");
  check(FootballFixtures.parse(feed(event("Bad",":garbage","")),0).isEmpty(),"Malformed date excluded");
  check(FootballFixtures.parse(feed(event("Floating",":20261022T190000","")),0).isEmpty(),"Do not guess missing timezone");
  List<FootballFixtures.Match> allDay=FootballFixtures.parse(feed(event("Bayern - Rival",";VALUE=DATE:20261022","")),1);
  check(allDay.get(0).kickoff==null&&allDay.get(0).day(aruba).equals(day),"Unconfirmed time remains date-only");
  String folded=feed(event("Atlético Nacional -\r\n\r\n  Rival\\, FC",":20261022T235900Z",""));
  check(FootballFixtures.parse(folded,3).get(0).title.equals("Atlético Nacional - Rival, FC"),"Folded lines and escaped comma");
  try{FootballFixtures.parse("<html>Error</html>",0);throw new AssertionError("HTML accepted");}catch(IllegalArgumentException expected){checks++;}
  check(FootballFixtures.forDay(a,day,ZoneId.of("Asia/Tokyo")).isEmpty(),"Device timezone changes day grouping");
  if(args.length>0)for(int i=0;i<5;i++){
   String body=Files.readString(Path.of(args[0],"football-"+FootballFixtures.SLUGS[i]+".ics"));List<FootballFixtures.Match> actual=FootballFixtures.parse(body,i);
   check(actual.size()>20,"Real subscription parsed for "+FootballFixtures.NAMES[i]);
   check(actual.stream().anyMatch(m->m.day(aruba).isAfter(day)),"Future fixtures present for "+FootballFixtures.NAMES[i]);
   System.out.println(FootballFixtures.NAMES[i]+": "+actual.size()+" valid fixtures");
  }
  System.out.println("PASS: "+checks+" checks");
 }
}
