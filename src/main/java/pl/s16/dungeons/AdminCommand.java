package pl.s16.dungeons;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.time.*;
import java.util.*;

/** Komendy sa przeznaczone dla administratorow; normalni gracze widza tylko publiczny status etapu. */
public final class AdminCommand implements CommandExecutor, TabCompleter {
    private final ManiusDungeons plugin;
    private final ScheduleStore schedules;
    private final LootStore loot;
    private final AdminMenu menu;
    private static final List<String> SUBS=List.of("pomoc","zaplanuj","usun","lista","kalendarz","pokoje","loot","szansa","podglad","status","reload");
    AdminCommand(ManiusDungeons p,ScheduleStore schedules,LootStore loot,AdminMenu menu) {
        this.plugin=p;this.schedules=schedules;this.loot=loot;this.menu=menu;
    }
    private void msg(CommandSender s,String text){s.sendMessage(plugin.prefix()+plugin.color(text));}
    @Override public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if(args.length==0){msg(sender,"&7System dungeonow jest w przygotowaniu. Wydarzenia nie sa jeszcze aktywne.");return true;}
        if(!args[0].equalsIgnoreCase("admin")){msg(sender,"&7Uzyj &f/dungeon admin &7(dla administratorow).");return true;}
        if(!sender.hasPermission("maniusdungeons.admin")){msg(sender,"&cBrak uprawnienia maniusdungeons.admin.");return true;}
        if(args.length==1){ if(sender instanceof Player p)menu.openMain(p);else help(sender);return true; }
        String cmd=args[1].toLowerCase(Locale.ROOT);
        try {switch(cmd){
            case "pomoc" -> help(sender);
            case "lista" -> {
                if(schedules.all().isEmpty())msg(sender,"&eBrak zaplanowanych wydarzen.");
                else for(DungeonSchedule s:schedules.all())msg(sender,"&b"+s.summary());
            }
            case "kalendarz" -> {
                if(sender instanceof Player p)menu.openCalendar(p);else msg(sender,"&cGUI dziala tylko dla graczy.");
            }
            case "loot" -> {
                if(args.length<3){msg(sender,"&eUzyj: /dungeon admin loot <slaby|dobry|boss|miniboss>");return true;}
                if(!(sender instanceof Player p)){msg(sender,"&cGUI wymaga gracza.");return true;}
                menu.openLoot(p,args[2].toLowerCase(Locale.ROOT));
            }
            case "szansa" -> {
                if(args.length!=5){msg(sender,"&eUzyj: /dungeon admin szansa <pool> <slot 1-45> <procent 0-100>");return true;}
                String pool=args[2].toLowerCase(Locale.ROOT);int slot=Integer.parseInt(args[3]);double chance=Double.parseDouble(args[4]);
                loot.setChance(pool,slot,chance);
                msg(sender,"&aZapisano szanse w "+pool+" na slocie "+slot+": "+chance+"%. &7(Silnik losowania w kolejnym etapie.)");
            }
            case "zaplanuj" -> {
                if(args.length<7||args.length>8){
                    msg(sender,"&eUzyj: /dungeon admin zaplanuj <id> <motyw> <RRRR-MM-DD|dni:MONDAY,FRIDAY> <HH:mm> <minuty> [losowo|15-100]");return true;
                }
                String id=args[2], theme=args[3].toLowerCase(Locale.ROOT);
                LocalDate date=null;EnumSet<DayOfWeek> days=EnumSet.noneOf(DayOfWeek.class);
                String dateArg=args[4];
                if(dateArg.toLowerCase(Locale.ROOT).startsWith("dni:")){
                    for(String part:dateArg.substring(4).split(","))days.add(DayOfWeek.valueOf(part.trim().toUpperCase(Locale.ROOT)));
                } else date=LocalDate.parse(dateArg);
                LocalTime at=LocalTime.parse(args[5]);int duration=Integer.parseInt(args[6]);
                int rooms=args.length==8&&!args[7].equalsIgnoreCase("losowo")?Integer.parseInt(args[7]):0;
                DungeonSchedule created=new DungeonSchedule(id,theme,date,days,at,duration,rooms);
                if(!schedules.add(created))msg(sender,"&cID juz istnieje lub nie udalo sie zapisac schedules.yml.");
                else msg(sender,"&aZapisano harmonogram: &f"+created.summary()+"&e (generator nadal nieaktywny)");
            }
            case "usun" -> {
                if(args.length!=3){msg(sender,"&eUzyj: /dungeon admin usun <id>");return true;}
                msg(sender,schedules.remove(args[2])?"&aUsunieto wydarzenie.":"&cNie znaleziono wydarzenia lub blad zapisu.");
            }
            case "pokoje" -> {
                if(args.length!=5){msg(sender,"&eUzyj: /dungeon admin pokoje <id> <RRRR-MM-DD> <15-100>");return true;}
                LocalDate day=LocalDate.parse(args[3]);int n=Integer.parseInt(args[4]);
                schedules.setRooms(args[2],day,n);
                msg(sender,"&aDzien "+day+", wydarzenie "+args[2]+": &f"+n+" pokoi.");
            }
            case "podglad" -> {
                if(args.length != 4) {msg(sender,"&eUzyj: /dungeon admin podglad <id> <RRRR-MM-DD>");return true;}
                DungeonSchedule event=schedules.get(args[2]);
                if(event==null)throw new IllegalArgumentException("Nieznane wydarzenie: "+args[2]);
                LocalDate day=LocalDate.parse(args[3]);
                if(!event.occursOn(day))throw new IllegalArgumentException("Wydarzenie nie przypada na wskazana date");
                int requested=schedules.rooms(event,day);
                long seed=Objects.hash(event.id,day);
                Random rng=new Random(seed);
                int roomCount=requested>0?requested:plugin.getConfig().getInt("safety.default-rooms-min",15)
                    +rng.nextInt(plugin.getConfig().getInt("safety.default-rooms-max",50)-plugin.getConfig().getInt("safety.default-rooms-min",15)+1);
                DungeonLayoutPlanner.Layout layout=DungeonLayoutPlanner.generate(roomCount,1+rng.nextInt(10),seed,
                    plugin.getConfig().getDouble("mobs.boss-chance-per-dungeon",0.05),
                    plugin.getConfig().getDouble("mobs.miniboss-chance-per-dungeon",0.25));
                msg(sender,"&bPodglad ukladu (bez zmiany swiata): &f"+event.id+" / "+day);
                msg(sender,"&7Pokoje: &f"+layout.rooms().size()+"&7, korytarze: &f"+layout.corridors().size()+"&7, wejscia: &f"+layout.entrances().size());
                msg(sender,"&7Wymiary planu: &f"+layout.width()+" x "+layout.length()+"&7; boss: &f"+layout.boss()+"&7; miniboss: &f"+layout.miniboss());
                msg(sender,"&eTo tylko plan geometryczny, nie gotowy ani wygenerowany dungeon.");
            }
            case "status" -> {
                msg(sender,"&eETAP 1: harmonogramy i edycja lootpooli dzialaja, ale generowanie, skrzynie w swiecie, bossowie, ochrona i BlueMap nie sa jeszcze aktywne.");
                msg(sender,"&7Strefa: &f"+plugin.timezone()+"&7; terminow: &f"+schedules.all().size());
            }
            case "reload" -> msg(sender,plugin.reloadAll()?"&aPonownie wczytano config.yml, schedules.yml i loot.yml.":"&cBlad reload; sprawdz konsole.");
            default -> help(sender);
        }} catch(Exception e) {msg(sender,"&cBlad polecenia: &f"+e.getMessage());}
        return true;
    }
    private void help(CommandSender s){
        msg(s,"&bKomendy manius_dungeons (TAB = podpowiedzi):");
        msg(s,"&f/dungeon admin &7- panel GUI");
        msg(s,"&f/dungeon admin zaplanuj <id> <motyw> <data|dni:...> <HH:mm> <minuty> [pokoje]");
        msg(s,"&f/dungeon admin lista &7- wszystkie wydarzenia");
        msg(s,"&f/dungeon admin kalendarz &7- GUI terminarza");
        msg(s,"&f/dungeon admin pokoje <id> <data> <15-100> &7- pokojow na konkretny dzien");
        msg(s,"&f/dungeon admin usun <id> &7- usuwa termin");
        msg(s,"&f/dungeon admin podglad <id> <data> &7- podglad ukladu bez budowania");
        msg(s,"&f/dungeon admin loot <slaby|dobry|boss|miniboss> &7- GUI przedmiotow");
        msg(s,"&f/dungeon admin szansa <pool> <slot> <0-100> &7- zapis szansy");
        msg(s,"&f/dungeon admin reload / status / pomoc");
        msg(s,"&eUWAGA: plugin nie generuje jeszcze dungeonow.");
    }
    @Override public List<String> onTabComplete(CommandSender sender,Command command,String alias,String[] args){
        if(args.length==1)return starts(List.of("admin"),args[0]);
        if(!sender.hasPermission("maniusdungeons.admin")||!args[0].equalsIgnoreCase("admin"))return List.of();
        if(args.length==2)return starts(SUBS,args[1]);
        String sub=args[1].toLowerCase(Locale.ROOT);
        List<String> choices=switch(sub){
            case "loot", "szansa" -> args.length==3?LootStore.POOLS:args.length==4&&sub.equals("szansa")?List.of("1","2","3","4","5","10","20","45"):args.length==5?List.of("0.01","1","5","10","25","50","100"):List.of();
            case "zaplanuj" -> switch(args.length){
                case 3 -> List.of("dungeon1");
                case 4 -> List.of("mechowy","siarkowy","pustynny","mesa");
                case 5 -> List.of(LocalDate.now(plugin.timezone()).toString(),"dni:MONDAY,WEDNESDAY,FRIDAY");
                case 6 -> List.of("17:00","18:00","19:00");
                case 7 -> List.of("60","120");
                case 8 -> List.of("losowo","15","20","30","50","100");
                default -> List.of();
            };
            case "usun", "pokoje", "podglad" -> args.length==3?schedules.all().stream().map(x->x.id).toList():args.length==4&&(sub.equals("pokoje")||sub.equals("podglad"))?List.of(LocalDate.now(plugin.timezone()).toString()):args.length==5?List.of("15","20","50","100"):List.of();
            default -> List.of();
        };
        return starts(choices,args[args.length-1]);
    }
    private List<String> starts(List<String> options,String typed){
        String lower=typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(o->o.toLowerCase(Locale.ROOT).startsWith(lower)).toList();
    }
}
