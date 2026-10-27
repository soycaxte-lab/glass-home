package local.glasshome;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

/** Local, searchable catalog of public calendar identifiers. No network access. */
final class FootballTeams {
 static final int MAX_FAVORITES=5;
 static final String PREFERENCE="football-teams-v1";
 static final class Team {
  final String id,name,group,searchKey,sortKey;
  Team(String id,String name,String group){this.id=id;this.name=name;this.group=group;this.searchKey=normalize(name+" "+group+" "+id);this.sortKey=normalize(name);}
  int badge(){for(int i=0;i<FootballFixtures.SLUGS.length;i++)if(id.equals(FootballFixtures.SLUGS[i]))return i;return -1;}
  public String toString(){return name+" · "+group;}
 }
 final List<Team> all;
 private final Map<String,Team> byId;
 FootballTeams(Reader reader)throws IOException {
  LinkedHashMap<String,Team> found=new LinkedHashMap<>();
  try(BufferedReader lines=new BufferedReader(reader)){
   String line;while((line=lines.readLine())!=null){if(line.startsWith("#")||line.startsWith("slug\t")||line.trim().isEmpty())continue;
    String[] fields=line.split("\t",-1);if(fields.length!=3||!fields[0].matches("[a-z0-9][a-z0-9-]{1,79}")||fields[1].trim().isEmpty()||fields[2].trim().isEmpty())throw new IOException("Invalid team catalog");
    found.putIfAbsent(fields[0],new Team(fields[0],fields[1],fields[2]));
   }
  }
  for(int i=0;i<FootballFixtures.SLUGS.length;i++){String id=FootballFixtures.SLUGS[i];found.putIfAbsent(id,new Team(id,FootballFixtures.NAMES[i],i==4?"Selecciones":"Favoritos originales"));}
  byId=Collections.unmodifiableMap(found);ArrayList<Team> sorted=new ArrayList<>(found.values());sorted.sort(Comparator.comparing(t->t.sortKey));all=Collections.unmodifiableList(sorted);
 }
 List<Team> defaults(){ArrayList<Team> result=new ArrayList<>();for(String id:FootballFixtures.SLUGS)result.add(byId.get(id));return Collections.unmodifiableList(result);}
 List<Team> restore(String stored){
  ArrayList<Team> result=new ArrayList<>();HashSet<String> seen=new HashSet<>();
  if(stored!=null)for(String id:stored.split(",")){Team t=byId.get(id.trim());if(t!=null&&seen.add(t.id)){result.add(t);if(result.size()==MAX_FAVORITES)break;}}
  return result.isEmpty()?defaults():Collections.unmodifiableList(result);
 }
 String serialize(Collection<Team> selected){
  if(selected.isEmpty()||selected.size()>MAX_FAVORITES)throw new IllegalArgumentException("Choose 1 to 5 teams");
  LinkedHashSet<String> ids=new LinkedHashSet<>();for(Team t:selected){if(t==null||!byId.containsKey(t.id)||!ids.add(t.id))throw new IllegalArgumentException("Invalid or duplicate team");}return String.join(",",ids);
 }
 List<Team> search(String query){String q=normalize(query.trim());ArrayList<Team> result=new ArrayList<>();for(Team t:all)if(q.isEmpty()||t.searchKey.contains(q))result.add(t);return result;}
 private static String normalize(String text){return Normalizer.normalize(text,Normalizer.Form.NFD).replaceAll("\\p{M}+","").toLowerCase(Locale.ROOT);}
}
