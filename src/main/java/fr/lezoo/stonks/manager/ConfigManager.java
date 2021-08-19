package fr.lezoo.stonks.manager;

import com.sun.org.apache.xpath.internal.operations.Quo;
import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.*;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.item.QuotationMap;
import fr.lezoo.stonks.item.SharePaper;
import fr.lezoo.stonks.item.TradingBook;
import fr.lezoo.stonks.quotation.QuotationTimeDisplay;
import fr.lezoo.stonks.share.ShareStatus;
import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.util.ConfigSchedule;
import fr.lezoo.stonks.util.message.Language;
import fr.lezoo.stonks.util.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;

public class ConfigManager {

    // List of items to reload
    public SharePaper sharePaper;
    public TradingBook tradingBook;
    public QuotationMap quotationMap;

    // Accessible public GUIs
    public final QuotationList QUOTATION_LIST = new QuotationList();
    public final QuotationShareMenu QUOTATION_SHARE = new QuotationShareMenu();
    public final PortfolioList OPEN_PORTFOLIO_LIST = new PortfolioList(ShareStatus.OPEN);
    public final PortfolioList CLOSED_PORTFOLIO_LIST = new PortfolioList(ShareStatus.CLOSED);
    public final SpecificPortfolio OPEN_SPECIFIC_PORTFOLIO = new SpecificPortfolio(ShareStatus.OPEN);
    public final SpecificPortfolio CLOSED_SPECIFIC_PORTFOLIO = new SpecificPortfolio(ShareStatus.CLOSED);
    public final ShareStatusMenu SHARE_STATUS_MENU = new ShareStatusMenu();

    private final EditableInventory[] guis = {QUOTATION_LIST, QUOTATION_SHARE, OPEN_PORTFOLIO_LIST, CLOSED_PORTFOLIO_LIST, OPEN_SPECIFIC_PORTFOLIO, CLOSED_SPECIFIC_PORTFOLIO,SHARE_STATUS_MENU};

    // Accessible public config fields
    public DecimalFormat stockPriceFormat, shareFormat;
    public DateFormat dateFormat;
    public ConfigSchedule closeTime, openTime;
    public boolean closeTimeEnabled;
    public List<String> displaySignFormat;
    public int dividendsRedeemHour;

    public long boardRefreshTime, quotationRefreshTime, shareRefreshTime;
    public double offerDemandImpact, volatility;
    public int quotationDataNumber, maxInteractionDistance;



    public void reload() {

        // Reload default config
        Stonks.plugin.reloadConfig();

        // Update public config fields
        dateFormat = new SimpleDateFormat(Stonks.plugin.getConfig().getString("date-format"));
        stockPriceFormat = new DecimalFormat(Stonks.plugin.getConfig().getString("stock-price-decimal-format"));
        shareFormat = new DecimalFormat(Stonks.plugin.getConfig().getString("shares-decimal-format"));
        boardRefreshTime = Stonks.plugin.getConfig().getLong("board-refresh-time");
        closeTimeEnabled = Stonks.plugin.getConfig().getBoolean("close-time.enabled");
        closeTime = new ConfigSchedule(Stonks.plugin.getConfig().getConfigurationSection("close-time.from"));
        openTime = new ConfigSchedule(Stonks.plugin.getConfig().getConfigurationSection("close-time.to"));
        offerDemandImpact = Stonks.plugin.getConfig().getDouble("offer-demand-impact");
        volatility = Stonks.plugin.getConfig().getDouble("volatility");
        displaySignFormat = Stonks.plugin.getConfig().getStringList("custom-sign-format");
        dividendsRedeemHour = Stonks.plugin.getConfig().getInt("dividends-redeem-hour");
        quotationDataNumber = Stonks.plugin.getConfig().getInt("quotation-data-number");
        quotationRefreshTime = QuotationTimeDisplay.QUARTERHOUR.getTime() / quotationDataNumber;
        shareRefreshTime = Stonks.plugin.getConfig().getLong("share-refresh-time");
        maxInteractionDistance = Stonks.plugin.getConfig().getInt("maxinteractiondistance");

        // Useful checks
        Validate.isTrue(displaySignFormat.size() == 4, "Display sign format should be of length 4");

        // Copy default files
        for (DefaultFile def : DefaultFile.values())
            def.checkFile();

        // Save default language file
        ConfigFile language = new ConfigFile("/language", "language");
        for (Language key : Language.values()) {
            String path = key.getPath();
            if (!language.getConfig().contains(path))
                language.getConfig().set(path, key.getCached());

            key.update(language.getConfig().getString(path));
        }
        language.save();

        // Save default messages
        ConfigFile messages = new ConfigFile("/language", "messages");
        for (Message key : Message.values()) {
            String path = key.getPath();
            if (!messages.getConfig().contains(path)) {
                messages.getConfig().set(path + ".format", key.getCached());
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
        sharePaper = new SharePaper(config.getConfigurationSection("PHYSICAL_SHARE_BILL"));
        quotationMap = new QuotationMap(config.getConfigurationSection("QUOTATION_MAP"));
        tradingBook = new TradingBook(config.getConfigurationSection("TRADING_BOOK"));


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
        BOARD("language","board.yml"),
        GUI_STATUS_TYPE_MENU("language/gui","share-status-menu.yml"),
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
