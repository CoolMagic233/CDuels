package me.coolmagic.cduels.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import me.coolmagic.cduels.Main;
import me.coolmagic.cduels.arenas.Arena;
import me.coolmagic.cduels.arenas.ArenaUtils;

public class MainCommands extends Command {
    public MainCommands(String name, String description) {
        super(name, description);
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        if (strings.length == 0) {
            if (Main.getInstance().getArenas().size() == 0) {
                commandSender.sendMessage("Don't have any arena!");
                return true;
            }
            for (Arena a : Main.getInstance().getArenas().values()) {
                if (a.getArenaStatus() == 1) {
                    if (commandSender instanceof Player) {
                        if ((a.getDiedPlayers().size() + a.getLivePlayers().size()) < 2) {
                            a.join((Player) commandSender);
                            commandSender.sendMessage(Main.getInstance().getMessage(a.getName(), "mate"));
                            return true;
                        }
                    } else {
                        commandSender.sendMessage("You must use command as a player!");
                        return true;
                    }
                }
            }
            commandSender.sendMessage("Don't have free arena.");
            return true;
        }
        if (strings.length == 2) {
            if (commandSender instanceof Player) {
                switch (strings[0]) {
                    case "join":
                        if (Main.getInstance().getArenaByName(strings[1]) != null) {
                            Main.getInstance().getArenaByName(strings[1]).join((Player) commandSender);
                            break;
                        }
                    case "quit":
                        if (Main.getInstance().getArenaByName(((Player) commandSender).getLevel().getName()) == null) {
                            commandSender.sendMessage("Error!");
                            return true;
                        }
                        if (Main.getInstance().getArenaByName(((Player) commandSender).getLevel().getName()).isPlaying((Player) commandSender)) {
                            Main.getInstance().getArenaByName(((Player) commandSender).getLevel().getName()).quit((Player) commandSender);
                        } else {
                            commandSender.sendMessage("You not in arena.");
                        }
                        break;
                }
            } else {
                commandSender.sendMessage("You must use command as a player!");
            }
        }
        //cduels duel sumo player : length.3
        if (strings.length == 3) {
            if (commandSender instanceof Player) {
                if (strings[0].equals("duel")) {
                    if (Main.getInstance().getServer().getPlayer(strings[2]) == null) {
                        commandSender.sendMessage(strings[2] + "not found.");
                        return true;
                    }
                    ArenaUtils.sendDuelModal(Main.getInstance().getServer().getPlayer(strings[2]), (Player) commandSender, strings[1]);
                }
            } else {
                commandSender.sendMessage("You must use command as a player!");
            }
        }
        //cduels duel
        if (strings.length == 1) {
            if (strings[0].equals("duel")) {
                if (commandSender instanceof Player) {
                    ArenaUtils.sendDuelPlayers((Player) commandSender);
                    return true;
                } else {
                    commandSender.sendMessage("You must use command as a player!");
                }
            }
        }
        return false;
    }
}
