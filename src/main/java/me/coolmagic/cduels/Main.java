package me.coolmagic.cduels;

import cn.nukkit.Player;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerFoodLevelChangeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
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
        File file4 = new File(getDataFolder() + "/Backup");
        if (!file4.exists() && !file4.mkdirs()) this.getLogger().error("Backup 文件夹初始化失败");
        this.getServer().getCommandMap().register("CDuels",new MainCommands("cduels","决斗游戏主命令"));
        this.getServer().getPluginManager().registerEvents( this,this);

        loadRooms();
        this.getServer().getScheduler().scheduleRepeatingTask(new ArenaThread(),20);
        this.getLogger().info("插件加载完成");
    }
    //监听器

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        if(Main.getInstance().getArenaByName(player.getLevel().getName()) == null) return;
        if(!Main.getInstance().getArenaByName(player.getLevel().getName()).isPlaying(player)) return;
        Arena arena = Main.getInstance().getArenaByName(player.getLevel().getName());
        if(arena.getArenaStatus() < 0) return;
        for(Arena a:Main.getInstance().getArenas().values()){
            if(a.getLivePlayers().contains(player) || a.getDiedPlayers().contains(player)){
                a.getLivePlayers().remove(player);
                a.getDiedPlayers().remove(player);
                a.sendMessageForArenasPlayers(Main.getInstance().getMessage(a.getName(),"quit").replace("@p",player.getName()).replace("@maxPlayer",String.valueOf(2)).replace("@minPlayer",String.valueOf(a.getLivePlayers().size() + a.getDiedPlayers().size())));
            }
        }
    }

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
        if(arena.getArenaStatus() == 2){
            if(e.getCause() == EntityDamageEvent.DamageCause.FALL){
                e.setCancelled();
            }
            if(e.getFinalDamage() + 1 >= e.getEntity().getHealth()){
                arena.onDeath(player);
            }
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
    public void onEbE(EntityDamageByEntityEvent e){
        if(!(e.getEntity() instanceof Player) || !(e.getDamager() instanceof Player)){
            return;
        }
        Player damager = (Player) e.getEntity();
        Player killer = (Player) e.getDamager();
        if(Main.getInstance().getArenaByName(damager.getLevel().getName()) == null) return;
        if(!Main.getInstance().getArenaByName(damager.getLevel().getName()).isPlaying(damager)) return;
        Arena arena = Main.getInstance().getArenaByName(damager.getLevel().getName());
        if(arena.getArenaStatus() < 0) return;
        if(e.getFinalDamage() + 1 >= damager.getHealth()){
            e.setCancelled();
            arena.onDeath(damager,killer);
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
    @EventHandler
    public void onFood(PlayerFoodLevelChangeEvent e){
        Player player = e.getPlayer();
        Arena arena = Main.getInstance().getArenaByName(player.getLevel().getName());
        if(arena == null) return;
        if(!arena.isPlaying(player)) return;
        if(arena.getArenaStatus() < 0) return;
        e.setCancelled();
    }
    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        Player player = e.getPlayer();
        Arena arena = Main.getInstance().getArenaByName(player.getLevel().getName());
        if(arena == null) return;
        if(!arena.isPlaying(player)) return;
        if(arena.getArenaStatus() < 0) return;
        if(arena.getArenaMode() == ArenaMode.BuildUhc){
            if(!arena.getWhiteListBlocks().contains(e.getBlock())){
                arena.getWhiteListBlocks().add(e.getBlock().clone());
                return;
            }
        }
        e.setCancelled();
    }
    @EventHandler
    public void onBreak(BlockBreakEvent e){
            Player player = e.getPlayer();
            Arena arena = Main.getInstance().getArenaByName(player.getLevel().getName());
            if(arena == null) return;
            if(!arena.isPlaying(player)) return;
            if(arena.getArenaStatus() < 0) return;
            if(arena.getArenaMode() == ArenaMode.BuildUhc){
                if (arena.getWhiteListBlocks().contains(e.getBlock())) {
                    arena.getWhiteListBlocks().remove(e.getBlock());
                    return;
                }
            }
            e.setCancelled();
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
        this.getLogger().info("开始识别Builduhc的房间");
        List<String> buildUhc = config.getStringList("BuildUhc");
        for(String s : buildUhc){
            if(this.getServer().getLevelByName(s) != null) {
                this.getLogger().info("识别到房间->" + s);
                arenas.put(s,new Arena(s,this.getServer().getLevelByName(s), ArenaMode.BuildUhc));
                arenaModeMap.put(ArenaMode.BuildUhc,new Arena(s,this.getServer().getLevelByName(s), ArenaMode.BuildUhc));
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

    public void unloadLevel(Level level) {
        getServer().unloadLevel(level);
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