package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.CustomItem;
import fr.lezoo.stonks.api.util.ConfigSchedule;
import fr.lezoo.stonks.api.util.message.Message;
import fr.lezoo.stonks.gui.QuotationList;
import fr.lezoo.stonks.gui.QuotationShareMenu;
import fr.lezoo.stonks.gui.api.EditableInventory;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class ConfigManager {

    // List of items to reload
    private final String[] itemIds = {"PHYSICAL_SHARE_BILL"};
    private final Map<String, CustomItem> items = new HashMap<>();

    // Accessible public GUIs
    public final QuotationList QUOTATION_LIST = new QuotationList();
    public final QuotationShareMenu QUOTATION_SHARE = new QuotationShareMenu();
    public final QuotationShareMenu PORTFOLIO_LIST = new QuotationShareMenu();
    public final QuotationShareMenu SPECIFIC_PORTFOLIO = new QuotationShareMenu();
    private final EditableInventory[] guis = {QUOTATION_LIST, QUOTATION_SHARE, PORTFOLIO_LIST, SPECIFIC_PORTFOLIO};

    // Accessible public config fields
    public DecimalFormat stockPriceFormat, shareFormat;
    public ConfigSchedule closeTime, openTime;
    public boolean closeTimeEnabled;

    public long boardRefreshTime, quotationRefreshTime;
    public double offerDemandImpact,volatility;


    public void reload() {

        // Reload default config
        Stonks.plugin.reloadConfig();

        // Update public config fields
        stockPriceFormat = new DecimalFormat(Stonks.plugin.getConfig().getString("stock-price-decimal-format"));
        shareFormat = new DecimalFormat(Stonks.plugin.getConfig().getString("shares-decimal-format"));
        boardRefreshTime = Stonks.plugin.getConfig().getLong("board-refresh-time");
        closeTimeEnabled = Stonks.plugin.getConfig().getBoolean("close-time.enabled");
        closeTime = new ConfigSchedule(Stonks.plugin.getConfig().getConfigurationSection("close-time.from"));
        openTime = new ConfigSchedule(Stonks.plugin.getConfig().getConfigurationSection("close-time.to"));
        quotationRefreshTime = Stonks.plugin.getConfig().getLong("quotation-refresh-time");
        offerDemandImpact=Stonks.plugin.getConfig().getDouble("offer-demand-impact");
        volatility=Stonks.plugin.getConfig().getDouble("volatility");
        // Copy default files
        for (DefaultFile def : DefaultFile.values())
            def.checkFile();

        // Save default messages
        ConfigFile messages = new ConfigFile("/language", "messages");
        for (Message key : Message.values()) {
            String path = key.getPath();
            if (!messages.getConfig().contains(path)) {
                messages.getConfig().set(path + ".format", key.getDefault());
                if (key.hasSound()) {
                    messages.getConfig().set(path + ".sound.name", key.getSound().getSound().name());
                    messages.getConfig().set(path + ".sound.vol", key.getSound().getVolume());
                    messages.getConfig().set(path + ".sound.pitch", key.getSound().getPitch());
                }
            }

            key.update(messages.getConfig().getConfigurationSection(path));
        }
        messages.save();

        // Reload messages
        FileConfiguration messagesConfig = new ConfigFile("/language", "messages").getConfig();
        for (Message message : Message.values())
            try {
                message.update(messagesConfig.getConfigurationSection(message.getPath()));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not reload message " + message.name() + ": " + exception.getMessage());
            }

        // Reload items
        FileConfiguration config = new ConfigFile("/language", "items").getConfig();
        for (String id : itemIds)
            try {
                items.put(id, new CustomItem(config.getConfigurationSection(id)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load item '" + id + "': " + exception.getMessage());
            }

        // Reload GUIs
        for (EditableInventory inv : guis)
            try {
                inv.reload(new ConfigFile("/language/gui", inv.getId()).getConfig());
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load custom inventory '" + inv.getId() + "': " + exception.getMessage());
            }
    }

    /**
     * All config files that have a default configuration are stored here,
     * they are copied into the plugin folder when the plugin enables
     */
    public enum DefaultFile {
        ITEMS("language", "items.yml"),
        QUOTATIONS("", "quotations.yml"),

        GUI_QUOTATION_LIST("language/gui", "quotation-list.yml"),
        GUI_SHARE_MENU("language/gui", "share-menu.yml"),
        GUI_PORTFOLIO_LIST("language/gui", "portfolio-list.yml"),
        GUI_SPECIFIC_PORTFOLIO("language/gui", "specific-portfolio.yml"),
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
            return new File(Stonks.plugin.getDataFolder() + "/" + folderName, fileName);
        }

        private String getResourcePath() {
            return "default/" + (folderName.isEmpty() ? "" : folderName + "/") + fileName;
        }

        public void checkFile() {

            // Check folder first
            File folder = new File(Stonks.plugin.getDataFolder() + "/" + folderName);
            if (!folder.exists())
                folder.mkdir();

            // Check file
            File file = getFile();
            if (!file.exists())
                try {
                    Files.copy(Stonks.plugin.getResource(getResourcePath()), file.getAbsoluteFile().toPath());
                } catch (IOException exception) {
                    Stonks.plugin.getLogger().log(Level.WARNING, "Could not load default file " + name() + ": " + exception.getMessage());
                }
        }
    }
}
