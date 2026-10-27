package local.glasshome;
import java.io.*;import java.nio.file.*;import java.util.*;
public final class FootballTeamsCheck {
 private static int checks;
 private static void check(boolean ok,String message){checks++;if(!ok)throw new AssertionError(message);}
 private static void reject(Runnable r,String message){try{r.run();throw new AssertionError(message);}catch(IllegalArgumentException expected){checks++;}}
 public static void main(String[] args)throws Exception{
  FootballTeams catalog=new FootballTeams(Files.newBufferedReader(Path.of("app/src/main/assets/football-teams.tsv")));
  check(catalog.all.size()>200,"Catalog missing teams");
  check(catalog.serialize(catalog.defaults()).equals(String.join(",",FootballFixtures.SLUGS)),"Upgrade must retain original favorites in order");
  check(catalog.restore(null).size()==5,"First installation defaults");
  check(catalog.restore("missing").size()==5,"Corrupt preference fallback");
  check(catalog.restore("co,co,real-madrid").size()==2,"Duplicate preferences removed");
  check(catalog.restore("co,missing,real-madrid").get(0).id.equals("co"),"Valid favorites and order retained");
  check(catalog.search("atletico nacional").stream().anyMatch(t->t.id.equals("atletico-nacional")),"Accent-insensitive search");
  check(catalog.search("seleccion de colombia").stream().anyMatch(t->t.id.equals("co")),"National team search");
  check(catalog.search("liverpool").size()>=2,"Same-name teams must remain distinct");
  check(catalog.search("liverpool inglaterra").size()==1,"Country must disambiguate team search");
  check(catalog.search("qzx-nonexistent").isEmpty(),"Empty search results");
  List<FootballTeams.Team> two=catalog.restore("co,real-madrid");check(catalog.serialize(two).equals("co,real-madrid"),"Preference round trip");
  check(two.get(0).badge()==4&&two.get(1).badge()==0,"Badges follow identity, not selected position");
  FootballTeams.Team extra=catalog.all.stream().filter(t->t.badge()<0).findFirst().get();check(extra.badge()==-1,"New teams must not use an unrelated badge");
  reject(()->catalog.serialize(Collections.emptyList()),"Empty choice accepted");
  reject(()->catalog.serialize(Arrays.asList(two.get(0),two.get(0))),"Duplicate choice accepted");
  reject(()->catalog.serialize(new ArrayList<>(catalog.all.subList(0,6))),"More than five accepted");
  reject(()->catalog.serialize(Collections.singletonList(new FootballTeams.Team("../escape","Invalid","Invalid"))),"Unsafe identifier accepted");
  try{new FootballTeams(new StringReader("../escape\tBad\tTest\n"));throw new AssertionError("Unsafe catalog accepted");}catch(IOException expected){checks++;}
  check(new HashSet<>(catalog.search("")).size()==catalog.all.size(),"Full search must not duplicate teams");
  System.out.println("PASS: "+checks+" team catalog, migration, validation, search and badge checks");
 }
}
