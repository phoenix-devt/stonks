package fr.lezoo.stonks.api;

import fr.lezoo.stonks.Stonks;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Util class to check out, edit and save configuration files
 */
public class ConfigFile {
    private final File file;
    private final FileConfiguration config;



    /**
     * @param name File name WITHOUT EXTENSION like "quotations"
     */
    public ConfigFile(String name) {
        this(Stonks.plugin, "", name);
    }

    /**
     * @param folder Folder path like "/language"
     * @param name   File name WITHOUT EXTENSION like "messages"
     */
    public ConfigFile(String folder, String name) {
        this(Stonks.plugin, folder, name);
    }

    /**
     * @param plugin Plugin owning the config file
     * @param folder Folder path like "/language"
     * @param name   File name WITHOUT EXTENSION like "messages"
     */
    private ConfigFile(Plugin plugin, String folder, String name) {
        config = YamlConfiguration.loadConfiguration(file = new File(plugin.getDataFolder() + folder, name + ".yml"));
    }

    /**
     * @return The file configuration which can actually
     * be checked out and edited if necessary
     */
    public FileConfiguration getConfig() {
        return config;
    }

    /**
     * Saves the edited config file
     */
    public void save() {
        try {
            config.save(file);
        } catch (IOException exception) {
            Stonks.plugin.getLogger().log(Level.SEVERE, "Could not save " + file.getName() + ".yml: " + exception.getMessage());
        }
    }
}