package scoreboard;

import scoreboard.base.IScoreboard;
import cn.nukkit.Server;

/**
 * @author lt_name
 */
public class ScoreboardUtil {

    private static IScoreboard scoreboard;

    private ScoreboardUtil() {

    }

    public synchronized static IScoreboard getScoreboard() {
        if (scoreboard == null) {
                try {
                    Class.forName("de.theamychan.scoreboard.ScoreboardPlugin");
                    if (Server.getInstance().getPluginManager().getPlugin("ScoreboardPlugin").isDisabled()) {
                        throw new Exception("Not Loaded");
                    }
                    scoreboard = new scoreboard.theamychan.SimpleScoreboard();
                } catch (Exception e1) {
                    scoreboard = new scoreboard.ltname.SimpleScoreboard();
                }
        }
        return scoreboard;
    }

}
