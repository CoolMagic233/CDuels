package me.coolmagic.cduels.arenas;

import cn.lanink.gamecore.form.windows.AdvancedFormWindowCustom;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import me.coolmagic.cduels.Main;

import java.util.Map;

public class ArenaUtils {
    private static Main duels;
    public static void sendDuelModal(Player receiver, Player inviter, String arenaMode) {
        duels = Main.getInstance();
        if(receiver.getName().equals(inviter.getName())){
            inviter.sendMessage("[" + duels.getPrefix() + "] 你不能发送邀请自己.");
            return;
        }
        if(arenaMode.equals("sumo")){
            AdvancedFormWindowModal modal = new AdvancedFormWindowModal(duels.getPrefix(), inviter.getName() + "邀请你在" + arenaMode + "激情决斗", "同意", "拒绝");
            modal.onClickedTrue(player1 -> {
                for (Map.Entry<ArenaMode, Arena> entry : duels.getArenaModeMap().entrySet()) {
                    if(String.valueOf(entry.getKey()).toLowerCase().equals(arenaMode)){
                        Arena arena = entry.getValue();
                        if ((arena.getArenaStatus() == 1) && arena.getAllPlayerCount() == 0) {
                            duels.getServer().dispatchCommand(player1,"cduels join "+arena.getName());
                            duels.getServer().dispatchCommand(inviter,"cduels join "+arena.getName());
                            return;
                        }
                    }
                }
                player1.sendMessage("[" + duels.getPrefix() +"] 没有空余的闲置房间!");
            });
            modal.onClickedFalse(player1 -> {
                player1.sendMessage("[" + duels.getPrefix() + "] 已拒绝决斗请求.");
                inviter.sendMessage("[" + duels.getPrefix() + "] "+player1.getName() + "已同意你的决斗请求.");
            });
            receiver.showFormWindow(modal);
            return;
        }
        inviter.sendMessage("[" + duels.getPrefix() + "] 未找到此模式 :" +arenaMode);
    }

    public static void sendDuelPlayers(Player player) {
        duels = Main.getInstance();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(duels.getPrefix());
            custom.addElement(new ElementInput("Please input player id."));
            custom.addElement(new ElementInput("Please input duel mode."));
            custom.onResponded((formResponseCustom, cp) -> {
                Player player1 = duels.getServer().getPlayer(formResponseCustom.getInputResponse(0));
                if (player1 == null || !(cp.getLevel().getName().equals(player1.getLevel().getName()))) {
                    cp.sendMessage("Player "+ formResponseCustom.getInputResponse(0) + formResponseCustom.getInputResponse(0) + " not online or not found.");
                } else {
                    for (Arena room : duels.getArenas().values()) {
                        if (!room.isPlaying(player1)) {
                            duels.getInstance().getServer().dispatchCommand(cp, "cduels duel "+ formResponseCustom.getInputResponse(1) + " " + formResponseCustom.getInputResponse(0));
                            return;
                        }
                        cp.sendMessage("This player is playing.");
                    }
                }
            });
            player.showFormWindow(custom);
    }
}
