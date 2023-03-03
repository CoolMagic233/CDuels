package me.coolmagic.cduels.arenas;

import cn.nukkit.Player;
import cn.nukkit.level.Level;
import me.coolmagic.cduels.Main;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private String name;
    private Level level;
    private List<Player> livePlayers = new ArrayList<>();
    private List<Player> diedPlayers = new ArrayList<>();
    
    public void join(Player player){}
    public void onDeath(Player player){
        livePlayers.remove(player);
        diedPlayers.add(player);
    }
    public void onDeath(Player died,Player killer){

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
