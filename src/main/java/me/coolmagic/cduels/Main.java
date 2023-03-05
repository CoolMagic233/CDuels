package me.coolmagic.cduels;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import me.coolmagic.cduels.arenas.Arena;
import me.coolmagic.cduels.arenas.ArenaMode;
import me.coolmagic.cduels.arenas.ArenaThread;
import me.coolmagic.cduels.commands.MainCommands;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends PluginBase implements Listener{
    private Map<String, Arena> arenas = new HashMap<>();
    private Map<ArenaMode, Arena> arenaModeMap = new HashMap<>();
    private static Config config;
    private static Main plugin;
    private static String prefix;
    @Override
    public void onEnable(){
        this.saveDefaultConfig();
        plugin = this;
        config = new Config(getDataFolder() + File.separator + "config.yml",Config.YAML);
        this.getServer().getCommandMap().register("CDuels",new MainCommands("cduels","决斗游戏主命令"));
        this.getServer().getPluginManager().registerEvents( this,this);
        loadRooms();
        this.getServer().getScheduler().scheduleRepeatingTask(new ArenaThread(),20);
        this.getLogger().info("插件加载完成");
    }
    //监听器
    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(!(e.getEntity() instanceof Player)){
            return;
        }
        Player player = (Player) e.getEntity();
        if(Main.getInstance().getArenaByName(player.getLevel().getName()) == null) return;
        if(!Main.getInstance().getArenaByName(player.getLevel().getName()).isPlaying(player)) return;
        Arena arena = Main.getInstance().getArenaByName(player.getLevel().getName());
        if(arena.getArenaStatus() < 0) return;
        if(arena.getArenaStatus() == 1){
            e.setCancelled();
            if(e.getCause() == EntityDamageEvent.DamageCause.VOID){
                player.teleport(player.getLevel().getSafeSpawn());
            }
            return;
        }
        if(arena.getArenaMode() == ArenaMode.Sumo){
            if(e.getCause() == EntityDamageEvent.DamageCause.VOID){
                e.setCancelled();
                arena.onDeath(player);
            }
            e.setDamage(0);
        }
    }
    @EventHandler
    public void onInv(PlayerInteractEvent e){
        Player player = e.getPlayer();
        Item handItem = player.getInventory().getItemInHand();
        if(Main.getInstance().getArenaByName(player.getLevel().getName()) == null) return;
        if(!Main.getInstance().getArenaByName(player.getLevel().getName()).isPlaying(player)) return;
        Arena arena = Main.getInstance().getArenaByName(player.getLevel().getName());
        if(arena.getArenaStatus() < 0) return;
        if(handItem.getId() == 341 && handItem.getCustomName().equals("Quit Arena")) arena.quit(player);
    }

    public Map<ArenaMode, Arena> getArenaModeMap() {
        return arenaModeMap;
    }

    public void loadRooms(){
        prefix = config.getString("prefix");
        //加载Sumo房间
        this.getLogger().info("开始识别房间");
        this.getLogger().info("开始识别Sumo的房间");
        List<String> sumo = config.getStringList("Sumo");
        for(String s : sumo){
            if(this.getServer().getLevelByName(s) != null) {
                this.getLogger().info("识别到房间->" + s);
                arenas.put(s,new Arena(s,this.getServer().getLevelByName(s), ArenaMode.Sumo));
                arenaModeMap.put(ArenaMode.Sumo,new Arena(s,this.getServer().getLevelByName(s), ArenaMode.Sumo));
            }else {
                this.getLogger().warning("房间"+s+"所占用的世界不存在!");
                this.getServer().shutdown();
            }
        }
        this.getLogger().info("已识别房间"+arenas.size()+"个");
    }
    public String getMessage(String arena,String key){
        return config.getString(arena+".message."+key);
    }
    public List<String> getCommandList(String arena,String type){
        switch (type){
            case "vCmd": return config.getStringList(arena+".vCmd");
            case "dCmd": return config.getStringList(arena+".dCmd");
            default: return new ArrayList<String>();
        }
    }
    public static Main getInstance(){
        return plugin;
    }
    public String getPrefix(){
        return prefix;
    }
    public Position strToPos(String key, Level level){
        return new Position(Integer.parseInt(key.split(":")[0]),Integer.parseInt(key.split(":")[1]),Integer.parseInt(key.split(":")[2]),level);
    }
    public Position strToPos(String key){
        return new Position(Integer.parseInt(key.split(":")[0]),Integer.parseInt(key.split(":")[1]),Integer.parseInt(key.split(":")[2]));
    }
    public Config getArenaConfig(){
        return config;
    }
    public Arena getArenaByName(String key){
        return arenas.getOrDefault(key,null);
    }
    public Map<String, Arena> getArenas(){
        return arenas;
    }
}