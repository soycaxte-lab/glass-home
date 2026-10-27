package local.glasshome;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Small iCalendar reader for the selected public subscriptions; no private app data. */
final class FootballFixtures {
 static final String[] SLUGS={"real-madrid","bayern-munchen","fc-barcelona","atletico-nacional","co"};
 static final String[] NAMES={"Real Madrid","Bayern Múnich","Barcelona","Atlético Nacional","Colombia"};
 static final class Match {
  final String title,competition; final Instant kickoff; final LocalDate dateOnly; final int team;
  Match(String title,String competition,Instant kickoff,LocalDate dateOnly,int team){this.title=title;this.competition=competition;this.kickoff=kickoff;this.dateOnly=dateOnly;this.team=team;}
  LocalDate day(ZoneId zone){return kickoff==null?dateOnly:kickoff.atZone(zone).toLocalDate();}
  String key(){return title.toLowerCase(Locale.ROOT).replaceAll("\\s+"," ")+"|"+(kickoff==null?dateOnly:kickoff);}
 }
 static List<Match> parse(String body,int team){
  if(!body.contains("BEGIN:VCALENDAR")||!body.contains("END:VCALENDAR"))throw new IllegalArgumentException("Incomplete calendar");
  // Ignore blank lines added by some feed servers before unfolding RFC 5545 lines.
  ArrayList<String> lines=new ArrayList<>();
  for(String line:body.replace("\r\n","\n").replace('\r','\n').split("\n")){
   if(line.isEmpty())continue;
   if((line.startsWith(" ")||line.startsWith("\t"))&&!lines.isEmpty()){int last=lines.size()-1;lines.set(last,lines.get(last)+line.substring(1));}else lines.add(line);
  }
  ArrayList<Match> result=new ArrayList<>();Map<String,String> event=null;
  for(String line:lines){
   if(line.equals("BEGIN:VEVENT")){event=new HashMap<>();continue;}
   if(line.equals("END:VEVENT")){if(event!=null){Match m=match(event,team);if(m!=null)result.add(m);}event=null;continue;}
   if(event!=null){int colon=line.indexOf(':');if(colon>0){String field=line.substring(0,colon);String base=field.split(";",2)[0];if(base.equals("DTSTART"))event.put("dateParams",field);event.put(base,line.substring(colon+1));}}
  }
  return result;
 }
 private static Match match(Map<String,String> fields,int team){
  if("CANCELLED".equals(fields.get("STATUS")))return null;
  String title=unescape(fields.getOrDefault("SUMMARY","")).trim(),date=fields.get("DTSTART");
  if(title.isEmpty()||date==null)return null;
  String competition="";int bracket=title.lastIndexOf(" [");
  if(bracket>0&&title.endsWith("]")){competition=title.substring(bracket+2,title.length()-1);title=title.substring(0,bracket);}
  // Scores in the feed are historical, not a live score service.
  title=title.replaceFirst("\\s+\\(\\d+\\s*[-–]\\s*\\d+\\)$","");
  try{
   if(date.matches("\\d{8}"))return new Match(title,competition,null,LocalDate.parse(date,DateTimeFormatter.BASIC_ISO_DATE),team);
   DateTimeFormatter fmt=DateTimeFormatter.ofPattern(date.replace("Z","").length()==13?"yyyyMMdd'T'HHmm":"yyyyMMdd'T'HHmmss");
   LocalDateTime local=LocalDateTime.parse(date.endsWith("Z")?date.substring(0,date.length()-1):date,fmt);
   ZoneId zone=ZoneOffset.UTC;
   if(!date.endsWith("Z")){
    String params=fields.getOrDefault("dateParams","");int tz=params.indexOf("TZID=");
    // Do not guess timezone for floating values or unsupported custom zones.
    if(tz<0)return null;String id=params.substring(tz+5).split(";",2)[0].replace("\"","");zone=ZoneId.of(id);
   }
   return new Match(title,competition,local.atZone(zone).toInstant(),null,team);
  }catch(DateTimeException ex){return null;}
 }
 private static String unescape(String value){StringBuilder s=new StringBuilder();for(int i=0;i<value.length();i++){char c=value.charAt(i);if(c=='\\'&&i+1<value.length()){char n=value.charAt(++i);s.append(n=='n'||n=='N'?' ':n);}else s.append(c);}return s.toString();}
 static List<Match> forDay(Collection<Match> all,LocalDate date,ZoneId zone){
  LinkedHashMap<String,Match> unique=new LinkedHashMap<>();
  for(Match m:all)if(date.equals(m.day(zone)))unique.putIfAbsent(m.key(),m);
  ArrayList<Match> result=new ArrayList<>(unique.values());result.sort(Comparator.comparing((Match m)->m.kickoff,Comparator.nullsLast(Comparator.naturalOrder())).thenComparing(m->m.title));return result;
 }
}
