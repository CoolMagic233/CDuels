package me.coolmagic.cduels.commands;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import me.coolmagic.cduels.Main;
import me.coolmagic.cduels.arenas.Arena;

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
            }
        }
        return false;
    }
}
