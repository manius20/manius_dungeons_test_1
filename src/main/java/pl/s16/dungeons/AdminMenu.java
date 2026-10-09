package pl.s16.dungeons;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;

import java.time.*;
import java.util.*;

/** GUI to zwykle Inventory Bukkit: administrator moze wkladac przedmioty do lootpooli. */
public final class AdminMenu implements Listener {
    private final ManiusDungeons plugin;private final ScheduleStore schedules;private final LootStore loot;
    AdminMenu(ManiusDungeons p,ScheduleStore s,LootStore l){plugin=p;schedules=s;loot=l;}
    private static final class MenuHolder implements InventoryHolder {
        final String type;
        private Inventory inventory;
        MenuHolder(String type){this.type=type;}
        void inventory(Inventory inv){this.inventory=inv;}
        @Override public Inventory getInventory(){return inventory;}
    }
    private Inventory create(String type,int size,String title){
        MenuHolder holder=new MenuHolder(type);Inventory inv=Bukkit.createInventory(holder,size,title);holder.inventory(inv);return inv;
    }
    private ItemStack icon(Material type,String title,String... lore){
        ItemStack stack=new ItemStack(type);ItemMeta meta=stack.getItemMeta();meta.setDisplayName("§b"+title);
        meta.setLore(Arrays.stream(lore).map(s->"§7"+s).toList());stack.setItemMeta(meta);return stack;
    }
    public void openMain(Player p){
        Inventory inv=create("main",27,"§9§lmanius_dungeons | Admin");
        inv.setItem(10,icon(Material.CLOCK,"Harmonogram", "Lista planowanych wydarzen", "Kliknij, aby zobaczyc kalendarz"));
        inv.setItem(12,icon(Material.CHEST,"Slaby loot", "Wstaw przedmioty do 45 slotow"));
        inv.setItem(13,icon(Material.DIAMOND_CHESTPLATE,"Dobry loot", "Wstaw przedmioty do 45 slotow"));
        inv.setItem(14,icon(Material.NETHERITE_SWORD,"Drop bossa", "Edycja dropow globalnych"));
        inv.setItem(15,icon(Material.IRON_SWORD,"Drop minibossa", "Edycja dropow globalnych"));
        inv.setItem(16,icon(Material.WRITABLE_BOOK,"Komendy", "Wpisz /dungeon admin pomoc", "TAB podpowiada dostepne polecenia"));
        inv.setItem(22,icon(Material.BARRIER,"Etap 1: generator nieaktywny", "Ten JAR nie generuje ani nie niszczy blokow", "Nigdy nie testuj generatora bez kopii swiata"));
        p.openInventory(inv);
    }
    public void openCalendar(Player p){
        Inventory inv=create("calendar",54,"§9§lmanius_dungeons | Kalendarz");
        List<DungeonSchedule> all=schedules.all();int slot=0;
        for(DungeonSchedule event:all){if(slot>=45)break;
            inv.setItem(slot++,icon(Material.PAPER,event.id,event.theme,event.recurrence(),"Start: "+event.start,"Czas: "+event.durationMinutes+" min","Pokoje: "+(event.rooms==0?"losowo":event.rooms)));
        }
        inv.setItem(49,icon(Material.BOOK,"Tworzenie terminu", "Na czacie: /dungeon admin pomoc", "/dungeon admin zaplanuj <...>"));
        p.openInventory(inv);
    }
    public void openLoot(Player p,String pool){
        if(!LootStore.POOLS.contains(pool))throw new IllegalArgumentException("Nieznany lootpool");
        Inventory inv=create("loot:"+pool,54,"§9§lLoot: "+pool);
        ItemStack[] contents=loot.contents(pool);
        for(int i=0;i<45;i++)inv.setItem(i,contents[i]);
        for(int i=45;i<54;i++)inv.setItem(i,icon(Material.GRAY_STAINED_GLASS_PANE,"§8——"));
        inv.setItem(49,icon(Material.WRITABLE_BOOK,"Zapisz: zamknij GUI", "Sloty 1-45 to przedmioty", "Szanse: /dungeon admin szansa <pool> <slot> <0-100>", "Przedmioty zostana zachowane z enchantami"));
        p.openInventory(inv);
    }
    @EventHandler public void click(InventoryClickEvent e){
        Inventory top=e.getView().getTopInventory();
        if(!(top.getHolder() instanceof MenuHolder h))return;
        if(!(e.getWhoClicked() instanceof Player player)||!player.hasPermission("maniusdungeons.admin")){e.setCancelled(true);return;}
        int slot=e.getRawSlot();
        if(h.type.startsWith("loot:")){
            if(slot>=45&&slot<54)e.setCancelled(true);
            // Pozostale sloty dzialaja jak zwykla skrzynia.
            return;
        }
        e.setCancelled(true);
        if(slot<0||slot>=top.getSize())return;
        if(h.type.equals("main")) switch(slot){
            case 10 -> openCalendar(player);
            case 12 -> openLoot(player,"slaby");
            case 13 -> openLoot(player,"dobry");
            case 14 -> openLoot(player,"boss");
            case 15 -> openLoot(player,"miniboss");
            case 16 -> {player.closeInventory();player.performCommand("dungeon admin pomoc");}
            default -> {}
        };
    }
    @EventHandler public void close(InventoryCloseEvent e){
        Inventory inv=e.getView().getTopInventory();
        if(!(inv.getHolder() instanceof MenuHolder h)||!h.type.startsWith("loot:"))return;
        if(!e.getPlayer().hasPermission("maniusdungeons.admin"))return;
        String pool=h.type.substring(5);
        ItemStack[] slots=new ItemStack[45];for(int i=0;i<45;i++)slots[i]=inv.getItem(i);
        try{loot.update(pool,slots);e.getPlayer().sendMessage(plugin.prefix()+"§aZapisano zawartosc lootpoolu §f"+pool+"§a.");}
        catch(Exception ex){e.getPlayer().sendMessage(plugin.prefix()+"§cBlad zapisu lootpoolu: "+ex.getMessage());}
    }
}
