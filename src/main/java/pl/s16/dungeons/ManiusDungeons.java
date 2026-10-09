package pl.s16.dungeons;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;

/** Bootstrap. Etap 1 nie modyfikuje zadnych blokow swiata. */
public final class ManiusDungeons extends JavaPlugin {
    private ScheduleStore schedules;
    private LootStore loot;
    private AdminMenu menu;
    private ScheduleTicker ticker;

    @Override public void onEnable() {
        saveDefaultConfig();
        try { ZoneId.of(getConfig().getString("settings.timezone", "Europe/Warsaw")); }
        catch (DateTimeParseException e) {
            getLogger().severe("Nieprawidlowa strefa czasowa w config.yml; plugin nie zostanie uruchomiony: " + e.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        schedules = new ScheduleStore(this);
        loot = new LootStore(this);
        menu = new AdminMenu(this, schedules, loot);
        AdminCommand admin = new AdminCommand(this, schedules, loot, menu);
        PluginCommand command = getCommand("dungeon");
        if (command == null) throw new IllegalStateException("Brak komendy dungeon w plugin.yml");
        command.setExecutor(admin);
        command.setTabCompleter(admin);
        Bukkit.getPluginManager().registerEvents(menu, this);
        ticker = new ScheduleTicker(this, schedules);
        // 1 minuta; operacje lekkie, wylacznie odczyt danych i komunikaty.
        ticker.start();
        getLogger().warning("ETAP 1: planowanie, GUI i lootpool sa dostepne. GENERATOR DUNGEONOW JEST JESZCZE NIEAKTYWNY; plugin nie zmienia swiata.");
    }
    @Override public void onDisable() {
        if (ticker != null) ticker.stop();
        if (schedules != null) schedules.save();
        if (loot != null) loot.save();
    }
    public String prefix() { return color(getConfig().getString("settings.prefix", "&9&lS16 SMP 2 &8» &r")); }
    public String color(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
    public ZoneId timezone() { return ZoneId.of(getConfig().getString("settings.timezone", "Europe/Warsaw")); }
    public FileConfiguration settings() { return getConfig(); }
    public boolean reloadAll() {
        try { reloadConfig(); timezone(); schedules.reload(); loot.reload(); return true; }
        catch (Exception e) { getLogger().severe("Blad reload: " + e.getMessage()); return false; }
    }
}
