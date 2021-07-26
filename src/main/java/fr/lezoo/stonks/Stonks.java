package fr.lezoo.stonks;

import fr.lezoo.stonks.api.util.ConfigSchedule;
import fr.lezoo.stonks.command.BoardDisplayCommand;
import fr.lezoo.stonks.command.StonksCommand;
import fr.lezoo.stonks.command.completion.RemoveBoardCommand;
import fr.lezoo.stonks.command.completion.StonksCommandCompletion;
import fr.lezoo.stonks.comp.placeholder.DefaultPlaceholderParser;
import fr.lezoo.stonks.comp.placeholder.PlaceholderAPIParser;
import fr.lezoo.stonks.comp.placeholder.PlaceholderParser;
import fr.lezoo.stonks.comp.placeholder.StonksPlaceholders;
import fr.lezoo.stonks.listener.PlayerListener;
import fr.lezoo.stonks.manager.*;
import fr.lezoo.stonks.version.ServerVersion;
import fr.lezoo.stonks.version.wrapper.VersionWrapper;
import fr.lezoo.stonks.version.wrapper.VersionWrapper_1_17_R1;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public ServerVersion version;
    public final ConfigManager configManager = new ConfigManager();
    public final ShareManager shareManager = new ShareManager();
    public Economy economy;

    // TODO fixer l'initialisation de ces classes
    public VersionWrapper versionWrapper = new VersionWrapper_1_17_R1();
    public PlayerDataManager playerManager = new PlayerDataManager();
    public QuotationManager quotationManager = new QuotationManager();
    public BoardManager boardManager = new BoardManager();

    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {

        // Read server version
        try {
            version = new ServerVersion(Bukkit.getServer().getClass());
            getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
        } catch (Exception exception) {
            getLogger().log(Level.INFO, ChatColor.RED + "Your server version is not compatible.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Vault economy
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        } else {
            getLogger().log(Level.SEVERE, "Could not hook onto Vault, disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Metrics data TODO change once plugin is posted on Spigot
        /*new Metrics(this, 111111);*/

        // Update checker, TODO change when plugin is on Spigot
        /*new SpigotPlugin(11111, this).checkForUpdate();*/

        // Reload config BEFORE config is reloaded
        saveDefaultConfig();

        // Initialize managers
        configManager.reload();
        quotationManager.load();
        shareManager.load();
        boardManager.load();
        playerManager.load();

        // PlaceholderAPI compatibility
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            new StonksPlaceholders().register();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        }

        // Register commands
        getCommand("stonks").setExecutor(new StonksCommand());
        getCommand("stonks").setTabCompleter(new StonksCommandCompletion());
        getCommand("boarddisplay").setExecutor(new BoardDisplayCommand());
        getCommand("removeboard").setExecutor(new RemoveBoardCommand());

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);

        // Create scheduler to refresh boards
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> boardManager.refreshBoards(), 0, 20L * this.configManager.boardRefreshTime);
    }

    @Override
    public void onDisable() {
        boardManager.save();
        playerManager.save();
        shareManager.save();
    }

    /**
     * @return If the stock market is closed and no shares
     * can be bought or closed
     */
    public boolean isClosed() {

        // If the stock market is always open
        if (!configManager.closeTimeEnabled)
            return false;

        // Make sure it's open otherwise
        return ConfigSchedule.isBetween(configManager.closeTime, configManager.openTime);
    }
}
