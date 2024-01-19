package com.magicpowered.rainbowseries;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class RainbowEdit extends JavaPlugin implements Listener {

    private FileManager fileManager;
    private CommandListener commandListener;
    private ItemEditor itemEditor;

    @Override
    public void onEnable() {
        try {

            fileManager = new FileManager(this);
            itemEditor = new ItemEditor(this, fileManager);
            commandListener = new CommandListener(this, fileManager, itemEditor);

            getCommand("re").setExecutor(commandListener);
            getCommand("re").setTabCompleter(commandListener);

            getServer().getPluginManager().registerEvents(this, this);
            getServer().getPluginManager().registerEvents(itemEditor, this);

            Bukkit.getServer().getLogger().info(" ");
            Bukkit.getServer().getLogger().info("  '||    ||' '||''|.         '||''|.   '||''''|     妙控动力 MagicPowered");
            Bukkit.getServer().getLogger().info("   |||  |||   ||   ||   ||   '||   ||   ||  .       彩虹系列 RainbowSeries");
            Bukkit.getServer().getLogger().info("   |'|..'||   ||...|'         ||''|'    ||''|       彩虹编辑 RainbowEdit v24.0.1.1");
            Bukkit.getServer().getLogger().info("   | '|' ||   ||        ||    ||   |.   ||          由 JLING 制作");
            Bukkit.getServer().getLogger().info("  .|. | .||. .||.            .||.  '|' .||.....|    https://magicpowered.cn");
            Bukkit.getServer().getLogger().info(" ");

        } catch (Exception e) {
            Bukkit.getServer().getLogger().info("[彩虹编辑] 启动失败!");
            e.printStackTrace();
        }

    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().getName().equalsIgnoreCase("RainbowEdit")) {
            Bukkit.getServer().getLogger().info("[彩虹编辑] 彩虹光照，世界依然，再会!");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (itemEditor.isInPreviewMode(player)) {
            itemEditor.cancelPreviewChanges(player);
        }
    }

    @Override
    public void onDisable() {
        fileManager.saveConfig();
    }


}
