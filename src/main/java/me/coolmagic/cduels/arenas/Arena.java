package me.coolmagic.cduels.arenas;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import me.coolmagic.cduels.Main;
import scoreboard.ScoreboardUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Arena {
    private String name;
    private Level level;
    private int arenaStatus = 1;
    private List<Player> livePlayers = new ArrayList<>();
    private List<Player> diedPlayers = new ArrayList<>();

    public List<Block> getWhiteListBlocks() {
        return whiteListBlocks;
    }

    private List<Block> whiteListBlocks = new ArrayList<>();
    private ArenaMode arenaMode;

    public int getArenaStatus() {
        return arenaStatus;
    }

    public void setArenaStatus(int arenaStatus) {
        // <0 房间未初始化, 1 房间等待玩家 , 2 房间进行中
        this.arenaStatus = arenaStatus;
    }

    public Arena(String name, Level level, ArenaMode arenaMode) {
        this.name = name;
        this.level = level;
        this.arenaMode = arenaMode;
    }

    public void setArenaMode(ArenaMode arenaMode) {
        this.arenaMode = arenaMode;
    }

    public ArenaMode getArenaMode() {
        return arenaMode;
    }

    public void join(Player player){
        if(arenaStatus != 1){
            player.sendMessage("["+Main.getInstance().getPrefix()+"] "+ "房间未初始化或已经开始了.");
            return;
        }
        if(livePlayers.contains(player) || diedPlayers.contains(player) ){
            player.sendMessage("["+Main.getInstance().getPrefix()+"] "+ "你已经在房间里了!");
            return;
        }
        setWait(player);
        Item quitItem = Item.get(341);
        quitItem.setCustomName("Quit Arena");
        player.getInventory().clearAll();
        player.getInventory().addItem(quitItem);
        player.teleport(level.getSafeSpawn());
        livePlayers.add(player);
        sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"join").replace("@p",player.getName()).replace("@maxPlayer",String.valueOf(2)).replace("@minPlayer",String.valueOf(livePlayers.size() + diedPlayers.size())));
    }
    public void onGameStart(){
        livePlayers.forEach(player -> {
            player.getInventory().clearAll();
            if(getArenaMode() == ArenaMode.BuildUhc){
                player.setGamemode(0);
                Item item8 = Item.get(310);
                Item item9 = Item.get(311);
                Item item10 = Item.get(312);
                Item item11 = Item.get(313);
                player.getInventory().setArmorItem(0, item8);
                player.getInventory().setArmorItem(1, item9);
                player.getInventory().setArmorItem(2, item10);
                player.getInventory().setArmorItem(3, item11);
                Item item12 = Item.get(276);
                Item item13 = Item.get(279);
                Item item14 = Item.get(322, 0, 5);
                Item item16 = Item.get(5, 0, 64);
                Item item17 = Item.get(261);
                Item item18 = Item.get(262, 0, 32);
                Item item19 = Item.get(368, 0, 5);
                Item item20 = Item.get(346);
                Item item21 = Item.get(325, 8);
                Item item22 = Item.get(325, 10);
                Item item24 = Item.get(368, 0, 16);
                player.getInventory().addItem(item24);
                player.getInventory().addItem(item12);
                player.getInventory().addItem(item13);
                player.getInventory().addItem(item14);
                player.getInventory().addItem(item16);
                player.getInventory().addItem(item17);
                player.getInventory().addItem(item18);
                player.getInventory().addItem(item19);
                player.getInventory().addItem(item20);
                player.getInventory().addItem(item21);
                player.getInventory().addItem(item22);
            }
        });
        setArenaStatus(2);

        livePlayers.get(0).teleport(Main.getInstance().strToPos(Main.getInstance().getArenaConfig().getString(name+".spawn.a"),level));
        livePlayers.get(1).teleport(Main.getInstance().strToPos(Main.getInstance().getArenaConfig().getString(name+".spawn.b"),level));
        sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"game_start"));
    }
    public boolean isPlaying(Player player){
        boolean playing = false;
        if (livePlayers.contains(player)) playing = true;
        if (diedPlayers.contains(player)) playing = true;
        return playing;
    }
    public void onDeath(Player player){
        sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"death_usual").replace("@p",player.getName()));
        setDied(player);
        livePlayers.remove(player);
        diedPlayers.add(player);
    }
    public void onDeath(Player died,Player killer){
        if(killer == null){
            onDeath(died);
            return;
        }
        sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"death").replace("@p",died.getName()).replace("@d", killer.getName()));
        setDied(died);
        livePlayers.remove(died);
        diedPlayers.add(died);
    }
    public void setWait(Player player){
        player.setGamemode(2);
        player.getInventory().clearAll();

        level.setRaining(false);
        level.setTime(12000);
        player.setHealth(20);
        player.getFoodData().setFoodLevel(40);
    }
    public void onGameEnd() throws Exception {
        for (Player player : livePlayers) {
            Main.getInstance().getCommandList(name, "vCmd").forEach(s -> Main.getInstance().getServer().dispatchCommand(new ConsoleCommandSender(), s.replace("@p", player.getName())));
            sendMessageForArenasPlayers(Main.getInstance().getMessage(name, "vic").replace("@p", player.getName()));
            player.setGamemode(2);
            player.getInventory().clearAll();
            ScoreboardUtil.getScoreboard().closeScoreboard(player);
            sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"quit").replace("@p",player.getName()).replace("@maxPlayer",String.valueOf(2)).replace("@minPlayer",String.valueOf(livePlayers.size() + diedPlayers.size())));
            player.teleport(Main.getInstance().getServer().getDefaultLevel().getSafeSpawn());
        }
        for (Player player : diedPlayers) {
            Main.getInstance().getCommandList(name, "dCmd").forEach(s -> Main.getInstance().getServer().dispatchCommand(new ConsoleCommandSender(), s.replace("@p", player.getName())));
            player.setGamemode(2);
            player.getInventory().clearAll();
            ScoreboardUtil.getScoreboard().closeScoreboard(player);
            sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"quit").replace("@p",player.getName()).replace("@maxPlayer",String.valueOf(2)).replace("@minPlayer",String.valueOf(livePlayers.size() + diedPlayers.size())));
            player.teleport(Main.getInstance().getServer().getDefaultLevel().getSafeSpawn());
        }
        setArenaStatus(-1);
        if (getArenaMode() == ArenaMode.BuildUhc && getArenaStatus() < 1) {
            Main.getInstance().getLogger().info("初始化地图ing ->" + getName());
            String old_world = "./worlds/" + this.level.getName();
            String new_world = "./plugins/CDuels/Backup/" + this.level.getName();
            if (Main.getInstance().getServer().getLevelByName(this.level.getName()) != null)
                Main.getInstance().unloadLevel(Main.getInstance().getServer().getLevelByName(this.level.getName()));
            File file = new File(new_world);
            File old_file = new File(old_world);
            if (!file.exists()) {
                Main.getInstance().getLogger().info("世界"+ getName() +"找不到备份文件");
                return;
            }
            ArenaUtils.toDelete(old_file);
            ArenaUtils.copyDir(file, new File("./worlds/"));
            if (Main.getInstance().getServer().loadLevel(getName())) {
                Main.getInstance().getLogger().info("加载世界 ->"+ getName() + "成功");
                this.level = Main.getInstance().getServer().getLevelByName(getName());
                for(FullChunk l:this.level.getChunks().values()){
                    l.unload();
                }
                setArenaStatus(1);
            } else {
                Main.getInstance().getLogger().info("加载世界 ->"+ getName() + "失败");
                Main.getInstance().getServer().shutdown();
            }
        }
        setArenaStatus(1);
        livePlayers.clear();
        diedPlayers.clear();
    }
    public void quit(Player player){
        player.setGamemode(2);
        player.getInventory().clearAll();
        diedPlayers.remove(player);
        livePlayers.remove(player);
        ScoreboardUtil.getScoreboard().closeScoreboard(player);
        sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"quit").replace("@p",player.getName()).replace("@maxPlayer",String.valueOf(2)).replace("@minPlayer",String.valueOf(livePlayers.size() + diedPlayers.size())));
        player.teleport(Main.getInstance().getServer().getDefaultLevel().getSafeSpawn());
    }
    public void setDied(Player player){
        player.setGamemode(3);
        Item quitItem = Item.get(341);
        quitItem.setCustomName("Quit Arena");
        player.getInventory().clearAll();
        player.getInventory().addItem(quitItem);
    }
    public void sendMessageForArenasPlayers(String message){
        livePlayers.forEach(player -> player.sendMessage("["+ Main.getInstance().getPrefix()+"] "+message));
        diedPlayers.forEach(player -> player.sendMessage("["+ Main.getInstance().getPrefix()+"] "+message));
    }
    public int getAllPlayerCount(){
        return (getLivePlayers().size() + getDiedPlayers().size());
    }

    public String getName() {
        return name;
    }

    public Level getLevel() {
        return level;
    }

    public List<Player> getLivePlayers() {
        return livePlayers;
    }

    public List<Player> getDiedPlayers() {
        return diedPlayers;
    }

}
