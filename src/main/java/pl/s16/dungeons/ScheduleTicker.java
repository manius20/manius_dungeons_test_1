package pl.s16.dungeons;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import java.time.*;
import java.util.*;

/** Tylko komunikaty informacyjne. Nie udaje, ze dungeon sie otworzyl. */
public final class ScheduleTicker {
    private final ManiusDungeons plugin;private final ScheduleStore store;
    private BukkitTask task;
    private final Map<String,String> notified=new HashMap<>();
    ScheduleTicker(ManiusDungeons p,ScheduleStore store){this.plugin=p;this.store=store;}
    void start(){task=Bukkit.getScheduler().runTaskTimer(plugin,this::tick,40L,1200L);}
    void stop(){if(task!=null)task.cancel();}
    private void tick(){
        ZonedDateTime now=ZonedDateTime.now(plugin.timezone());
        LocalDate today=now.toLocalDate();
        // Komunikaty o braku zaplanowanego wydarzenia sa informacyjne i pojawiaja sie raz na date.
        boolean daily=plugin.getConfig().getBoolean("settings.unplanned.daily-warning-enabled",true);
        boolean missing=plugin.getConfig().getBoolean("settings.unplanned.missing-slot-warning-enabled",true);
        String dailyTime=plugin.getConfig().getString("settings.unplanned.daily-warning-time","12:00");
        String slotTime=plugin.getConfig().getString("settings.unplanned.missing-slot-time","17:00");
        int advance=Math.max(1,plugin.getConfig().getInt("settings.unplanned.missing-slot-advance-minutes",60));
        try {
            if(daily&&now.toLocalTime().getHour()==LocalTime.parse(dailyTime).getHour()
                &&now.toLocalTime().getMinute()==LocalTime.parse(dailyTime).getMinute()
                &&store.on(today).isEmpty())once("daily:"+today,"&eNa dzis nie zaplanowano dungeonow. W razie pytan skontaktujcie sie z administracja!");
            LocalTime reminder=LocalTime.parse(slotTime).minusMinutes(advance);
            if(missing&&now.toLocalTime().getHour()==reminder.getHour()&&now.toLocalTime().getMinute()==reminder.getMinute()
                &&store.on(today).isEmpty())once("slot:"+today,"&eBrak dungeona na dzisiejsza godzine "+slotTime+". Napiszcie do administracji!");
        }catch(Exception ex){plugin.getLogger().warning("Nieprawidlowe godziny przypomnien w config.yml: "+ex.getMessage());}
        // W etapie 1 nie wysylamy falszywych powiadomien o otwarciu/zamknieciu.
        if(notified.size()>90)notified.clear();
    }
    private void once(String key,String msg){
        if(notified.putIfAbsent(key,key)!=null)return;
        Bukkit.broadcastMessage(plugin.prefix()+plugin.color(msg));
    }
}
