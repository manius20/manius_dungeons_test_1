package pl.s16.dungeons;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/** Zapis przedmiotow razem z enchantami, custom meta i wagami losowan. */
public final class LootStore {
    public static final List<String> POOLS=List.of("slaby", "dobry", "boss", "miniboss");
    private final ManiusDungeons plugin;
    private final File file;
    private final Map<String,ItemStack[]> pools=new HashMap<>();
    private final Map<String,Map<Integer,Double>> weights=new HashMap<>();
    LootStore(ManiusDungeons p){plugin=p;file=new File(p.getDataFolder(),"loot.yml");reload();}
    public synchronized void reload() {
        YamlConfiguration y=YamlConfiguration.loadConfiguration(file);
        pools.clear();weights.clear();
        for(String name:POOLS){
            ItemStack[] contents=new ItemStack[45];
            List<?> saved=y.getList("pools."+name+".items",List.of());
            for(int i=0;i<Math.min(45,saved.size());i++)if(saved.get(i) instanceof ItemStack item)contents[i]=item.clone();
            pools.put(name,contents);
            Map<Integer,Double> w=new HashMap<>();
            for(int i=0;i<45;i++){double chance=y.getDouble("pools."+name+".percent."+i,5.0);w.put(i,Math.max(0.0,Math.min(100.0,chance)));}
            weights.put(name,w);
        }
        if(!file.exists())save();
    }
    public synchronized ItemStack[] contents(String pool){
        ItemStack[] saved=pools.get(pool);if(saved==null)throw new IllegalArgumentException("Nieznany lootpool: "+pool);
        ItemStack[] result=new ItemStack[saved.length];for(int i=0;i<saved.length;i++)result[i]=saved[i]==null?null:saved[i].clone();return result;
    }
    public synchronized void update(String pool,ItemStack[] slots){
        if(!POOLS.contains(pool)||slots.length!=45)throw new IllegalArgumentException("Bledny lootpool/rozmiar");
        ItemStack[] old=contents(pool);ItemStack[] next=new ItemStack[45];
        for(int i=0;i<45;i++)next[i]=slots[i]==null?null:slots[i].clone();
        pools.put(pool,next);
        if(!save()){pools.put(pool,old);throw new IllegalStateException("Nie zapisano loot.yml");}
    }
    public synchronized void setChance(String pool,int slot,double chance){
        if(!POOLS.contains(pool)||slot<1||slot>45||chance<0||chance>100||!Double.isFinite(chance))throw new IllegalArgumentException("Pool, slot 1-45, szansa 0-100");
        Double old=weights.get(pool).put(slot-1,chance);
        if(!save()){weights.get(pool).put(slot-1,old);throw new IllegalStateException("Nie zapisano loot.yml");}
    }
    public synchronized double chance(String pool,int slot){return weights.getOrDefault(pool,Map.of()).getOrDefault(slot-1,0.0);}
    public synchronized boolean save(){
        YamlConfiguration y=new YamlConfiguration();
        for(String pool:POOLS){
            // YAML ItemStack serializer zachowuje meta i enchanty.
            y.set("pools."+pool+".items",Arrays.asList(pools.get(pool)));
            weights.get(pool).forEach((slot,percent)->y.set("pools."+pool+".percent."+slot,percent));
        }
        try{AtomicYaml.write(y,file);return true;}
        catch(IOException e){plugin.getLogger().log(Level.SEVERE,"Nie udalo sie zapisac loot.yml",e);return false;}
    }
}
