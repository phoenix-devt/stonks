package fr.lezoo.stonks.api;

import fr.lezoo.stonks.Stonks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class ConfigFile {
    private final File file;
    private final FileConfiguration config;

    public ConfigFile(Player player) {
        this(player.getUniqueId());
    }

    public ConfigFile(UUID uuid) {
        this(Stonks.plugin, "/userdata", uuid.toString());
    }

    public ConfigFile(String name) {
        this(Stonks.plugin, "", name);
    }

    public ConfigFile(String folder, String name) {
        this(Stonks.plugin, folder, name);
    }

    public ConfigFile(Plugin plugin, String folder, String name) {
        config = YamlConfiguration.loadConfiguration(file = new File(plugin.getDataFolder() + folder, name + ".yml"));
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            Stonks.plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName() + ".yml: " + exception.getMessage());
        }
    }
}