package me.coolmagic.cduels.arenas;

import cn.lanink.gamecore.form.windows.AdvancedFormWindowCustom;
import cn.lanink.gamecore.form.windows.AdvancedFormWindowModal;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementInput;
import me.coolmagic.cduels.Main;

import java.io.*;
import java.util.Map;
import java.util.Objects;

public class ArenaUtils {
    private static Main duels;
    public static void sendDuelModal(Player receiver, Player inviter, String arenaMode) {
        duels = Main.getInstance();
        if(receiver.getName().equals(inviter.getName())){
            inviter.sendMessage("[" + duels.getPrefix() + "] 你不能发送邀请自己.");
            return;
        }
        if(arenaMode.equals("sumo")){
            selectArena(receiver, inviter, arenaMode);
            return;
        }
        if(arenaMode.equals("builduhc")){
            selectArena(receiver, inviter, arenaMode);
            return;
        }
        inviter.sendMessage("[" + duels.getPrefix() + "] 未找到此模式 :" +arenaMode);
    }

    private static void selectArena(Player receiver, Player inviter, String arenaMode) {
        AdvancedFormWindowModal modal = new AdvancedFormWindowModal(duels.getPrefix(), inviter.getName() + "邀请你在" + arenaMode + "激情决斗", "同意", "拒绝");
        modal.onClickedTrue(player1 -> {
            for (Map.Entry<ArenaMode, Arena> entry : duels.getArenaModeMap().entrySet()) {
                if(String.valueOf(entry.getKey()).toLowerCase().equals(arenaMode)){
                    Arena arena = entry.getValue();
                    if ((arena.getArenaStatus() == 1) && arena.getAllPlayerCount() == 0) {
                        duels.getServer().dispatchCommand(player1,"cduels join "+arena.getName());
                        duels.getServer().dispatchCommand(inviter,"cduels join "+arena.getName());
                        player1.sendMessage("[" + duels.getPrefix() + "] 已同意决斗请求.");
                        inviter.sendMessage("[" + duels.getPrefix() + "] 已同意决斗请求.");
                        return;
                    }
                }
            }
            player1.sendMessage("[" + duels.getPrefix() +"] 没有空余的闲置房间!");
        });
        modal.onClickedFalse(player1 -> {
            player1.sendMessage("[" + duels.getPrefix() + "] 已拒绝决斗请求.");
            inviter.sendMessage("[" + duels.getPrefix() + "] "+player1.getName() + "已拒绝你的决斗请求.");
        });
        receiver.showFormWindow(modal);
    }

    public static void sendDuelPlayers(Player player) {
        duels = Main.getInstance();
        AdvancedFormWindowCustom custom = new AdvancedFormWindowCustom(duels.getPrefix());
            custom.addElement(new ElementInput("请输入玩家id."));
            custom.addElement(new ElementInput("请输入单挑模式."));
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
                        cp.sendMessage("此玩家正在游戏中.");
                    }
                }
            });
            player.showFormWindow(custom);
    }
    public static void toDelete(File file) {
        File[] files = file.listFiles();
        if (files != null)
            for (File file1 : files) {
                if (file1.isDirectory()) {
                    toDelete(file1);
                } else {
                    String str = file1.getName();
                    file1.delete();
                }
            }
        String name = file.getName();
    }

    public static void copyDir(File source,File targetDir) {
        if (source.isFile()) {
            copyFile(source, targetDir);
        }else {
            targetDir = new File(targetDir,source.getName());
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            File[] subFiles = source.listFiles();
            if (subFiles != null) {
                for (int i = 0; i < subFiles.length; i++) {
                    copyDir(subFiles[i], targetDir);
                }
            }
        }
    }

    public static void copyFile(File sourceFile,File targetDir) {
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        File target = new File(targetDir, sourceFile.getName());
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(sourceFile);
            os = new FileOutputStream(target);
            byte[] b = new byte[1024];
            int len = 0;
            Main.getInstance().getLogger().info("开始复制地图...");
            while ((len = is.read(b)) != -1) {
                os.write(b, 0, len);
            }
            Main.getInstance().getLogger().info("复制完成...");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (Objects.nonNull(os)) {
                    os.close();
                }
                if (Objects.nonNull(is)) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
