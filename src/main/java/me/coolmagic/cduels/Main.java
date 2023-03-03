package me.coolmagic.cduels;

import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.Config;
import me.coolmagic.cduels.arenas.Arena;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends PluginBase {
    private Map<String, Arena> arenas = new HashMap<>();
    private static Config config;
    private static Main plugin;
    private static String prefix;
    @Override
    public void onEnable(){
        this.saveDefaultConfig();
        plugin = this;
        config = new Config(getDataFolder() + File.separator + "config.yml",Config.YAML);
        loadRooms();
        this.getLogger().info("插件加载完成");
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
            }else {

            }
        }
    }
    public static Main getInstance(){
        return plugin;
    }
    public String getPrefix(){
        return prefix;
    }
}