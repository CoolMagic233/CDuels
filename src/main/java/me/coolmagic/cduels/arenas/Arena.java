package me.coolmagic.cduels.arenas;

import cn.nukkit.Player;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.nbt.tag.CompoundTag;
import me.coolmagic.cduels.Main;
import scoreboard.ScoreboardUtil;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private String name;
    private Level level;
    private int arenaStatus = 1;
    private List<Player> livePlayers = new ArrayList<>();
    private List<Player> diedPlayers = new ArrayList<>();
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
        if(arenaStatus < 1){
            setArenaStatus(1);
        }
        if(arenaStatus == 2){
            player.sendMessage("["+Main.getInstance().getPrefix()+"]"+ " This arena is started!");
            return;
        }
        if(livePlayers.contains(player) || diedPlayers.contains(player) ){
            player.sendMessage("You already in arena!");
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
    }
    public void onGameEnd(){
        livePlayers.forEach(player -> {
            Main.getInstance().getCommandList(name,"vCmd").forEach(s -> Main.getInstance().getServer().dispatchCommand(new ConsoleCommandSender(),s.replace("@p",player.getName())));
            sendMessageForArenasPlayers(Main.getInstance().getMessage(name,"vic").replace("@p",player.getName()));
            quit(player);
        });
        diedPlayers.forEach(player -> {
            Main.getInstance().getCommandList(name,"dCmd").forEach(s -> Main.getInstance().getServer().dispatchCommand(new ConsoleCommandSender(),s.replace("@p",player.getName())));
            quit(player);
        });
        setArenaStatus(-1);
        livePlayers.clear();
        diedPlayers.clear();
    }
    public void quit(Player player){
        player.setGamemode(2);
        player.getInventory().clearAll();
        ScoreboardUtil.getScoreboard().closeScoreboard(player);
        livePlayers.remove(player);
        diedPlayers.remove(player);
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
        livePlayers.forEach(player -> player.sendMessage("["+ Main.getInstance().getPrefix()+"]"+message));
        diedPlayers.forEach(player -> player.sendMessage("["+ Main.getInstance().getPrefix()+"]"+message));
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
