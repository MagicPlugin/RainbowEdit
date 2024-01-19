package com.magicpowered.rainbowseries;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class FileManager {
    private final RainbowEdit plugin;

    private FileConfiguration config;
    private File configFile;

    public FileManager(RainbowEdit plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConfigStream));
            config.setDefaults(defaultConfig);
        }
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }
        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public void saveDefaultConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }

    public String getMessage(String Key) {
        String rawPrefix = getConfig().getString("Message.prefix", "§7[§c彩虹编辑§7]");
        String rawMessage = getConfig().getString( "Message." + Key, "对于 " + Key + " 的消息未设置");
        return (rawPrefix + " " + rawMessage).replace("&", "§");
    }

    public String getActionBarMessage(int slot) {
        String rawMessage = getConfig().getString("ActionBar." + slot);
        return rawMessage.replace("&", "§");
    }
}
