package fr.lezoo.stonks;

import fr.lezoo.stonks.command.PortfolioCommand;
import fr.lezoo.stonks.command.QuotationsCommand;
import fr.lezoo.stonks.command.RedeemDividendsCommand;
import fr.lezoo.stonks.command.StonksCommandRoot;
import fr.lezoo.stonks.compat.placeholder.DefaultPlaceholderParser;
import fr.lezoo.stonks.compat.placeholder.PlaceholderAPIParser;
import fr.lezoo.stonks.compat.placeholder.PlaceholderParser;
import fr.lezoo.stonks.compat.placeholder.StonksPlaceholders;
import fr.lezoo.stonks.listener.DisplaySignListener;
import fr.lezoo.stonks.listener.PlayerListener;
import fr.lezoo.stonks.listener.SharePaperListener;
import fr.lezoo.stonks.listener.TradingInteractListener;
import fr.lezoo.stonks.manager.*;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
import fr.lezoo.stonks.quotation.api.StockAPI;
import fr.lezoo.stonks.util.ConfigSchedule;
import fr.lezoo.stonks.version.ServerVersion;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public final ConfigManager configManager = new ConfigManager();
    public final ShareManager shareManager = new ShareManager();
    public final SignManager signManager = new SignManager();
    public final PlayerDataManager playerManager = new PlayerDataManager();
    public final QuotationManager quotationManager = new QuotationManager();
    public final BoardManager boardManager = new BoardManager();

    @NotNull
    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    @NotNull
    public ServerVersion version;
    @Nullable
    public Economy economy;
    @Nullable
    public StockAPI stockAPI;

    /*
     * TODO Comment obtenir les lives de trading
     * TODO Signs, playerdata avec les shares
     */
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
        //load stockAPIManager
        quotationManager.load();
        shareManager.load();
        boardManager.load();
        signManager.load();
        playerManager.load();

        // PlaceholderAPI compatibility
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            new StonksPlaceholders().register();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        }

        // Register commands
        StonksCommandRoot commandRoot = new StonksCommandRoot();
        getCommand("stonks").setExecutor(commandRoot);
        getCommand("stonks").setTabCompleter(commandRoot);
        getCommand("redeemdividends").setExecutor(new RedeemDividendsCommand());
        getCommand("quotations").setExecutor(new QuotationsCommand());
        getCommand("portfolio").setExecutor(new PortfolioCommand());

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new SharePaperListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisplaySignListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradingInteractListener(), this);

        // Refresh the signs
        new BukkitRunnable() {

            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() > 0)
                    signManager.refreshSigns();
            }
        }.runTaskTimer(this, 0L, configManager.signRefreshTime);


        // Refresh boards
        new BukkitRunnable() {
            //We refresh the quotation by putting the good values
            @Override
            public void run() {

                quotationManager.refresh();
            }
        }.runTaskTimer(this, 0, TimeScale.MINUTE.getTime() / (1000 * Quotation.BOARD_DATA_NUMBER));

        // Refresh boards
        new BukkitRunnable() {
            //We refresh the board only if people are online.
            @Override
            public void run() {
                if (Bukkit.getOnlinePlayers().size() > 0)
                    boardManager.refreshBoards();
            }
        }.runTaskTimer(this, 0, 20L * this.configManager.boardRefreshTime);

        // Refresh the shares
        new BukkitRunnable() {

            @Override
            public void run() {
                shareManager.refresh();
            }
        }.runTaskTimer(this, 0L, configManager.shareRefreshTime);
    }


    @Override
    public void onDisable() {
        quotationManager.save();
        boardManager.save();
        playerManager.save();
        shareManager.save();
        signManager.save();
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
