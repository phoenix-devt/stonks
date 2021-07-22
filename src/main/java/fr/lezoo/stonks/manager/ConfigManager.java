package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.CustomItem;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {
    private final Map<String, CustomItem> items = new HashMap<>();
    private final String[] itemIds = {"PHYSICAL_STOCK_BILL"};

    public ConfigManager() {
        reload();
    }

    public void reload() {

        // Copy default files
        for (DefaultFile def : DefaultFile.values())
            def.checkFile();

        // Reload items
        FileConfiguration config = new ConfigFile("/language", "items").getConfig();
        for (String id : itemIds)
            items.put(id, new CustomItem(config.getConfigurationSection(id)));
    }

    /**
     * All config files that have a default configuration are stored here,
     * they are copied into the plugin folder when the plugin enables
     */
    public enum DefaultFile {
        ITEMS("language", "item.yml"),
        MESSAGES("language", "messages.yml"),
        QUOTATIONS("", "quotations.yml"),
        ;

        private final String folderName, fileName;

        /**
         * @param folderName Path of folder to put the file into
         * @param fileName   Name of file containing the extension
         */
        private DefaultFile(String folderName, String fileName) {
            this.folderName = folderName;
            this.fileName = fileName;
        }

        public File getFile() {
            return new File(Stonks.plugin.getDataFolder() + (folderName.equals("") ? "" : "/" + folderName), fileName);
        }

        public void checkFile() {
            File file = getFile();
            if (!file.exists())
                try {
                    Files.copy(Stonks.plugin.getResource((folderName.equals("") ? "" : folderName + "/") + fileName), file.getAbsoluteFile().toPath());
                } catch (IOException exception) {
                    Stonks.plugin.getLogger().log(Level.WARNING, "Could not load default file " + name() + ": " + exception.getMessage());
                }
        }
    }
}
