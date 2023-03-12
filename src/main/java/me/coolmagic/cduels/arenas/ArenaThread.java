package me.coolmagic.cduels.arenas;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.scheduler.Task;
import me.coolmagic.cduels.Main;
import scoreboard.ScoreboardUtil;
import scoreboard.base.IScoreboard;

import java.util.ArrayList;
import java.util.List;

public class ArenaThread extends Task {
    private static Main owner = Main.getInstance();

    @Override
    public void onRun(int i) {
        for(Arena a:owner.getArenas().values()){
            if(a.getArenaStatus() < 0) return;
            List<String> scoreboard = new ArrayList<>();
            for (String s:owner.getArenaConfig().getStringList(a.getName()+".scoreboard")) {
                scoreboard.add(s.replace("{livePlayers}",String.valueOf(a.getLivePlayers().size())));
            }
            for(Player p: a.getLivePlayers()){
                ScoreboardUtil.getScoreboard().showScoreboard(p,owner.getArenaConfig().getString(a.getName()+".scoreboard_title"),scoreboard);
            }
            for(Player p: a.getDiedPlayers()){
                ScoreboardUtil.getScoreboard().showScoreboard(p,owner.getArenaConfig().getString(a.getName()+".scoreboard_title"),scoreboard);
            }
            if(a.getArenaStatus() == 2) {
                if (a.getLivePlayers().size() <= 1) {
                    try {
                        a.onGameEnd();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if(a.getArenaStatus() == 1){
                if(a.getLivePlayers().size() > 1){
                    a.onGameStart();
                }
            }
        }
    }
}
