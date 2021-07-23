package fr.lezoo.stonks;

import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.command.StonksCommand;
import fr.lezoo.stonks.command.completion.StonksCommandCompletion;
import fr.lezoo.stonks.comp.placeholder.DefaultPlaceholderParser;
import fr.lezoo.stonks.comp.placeholder.PlaceholderAPIParser;
import fr.lezoo.stonks.comp.placeholder.PlaceholderParser;
import fr.lezoo.stonks.comp.placeholder.StonksPlaceholders;
import fr.lezoo.stonks.listener.PlayerListener;
import fr.lezoo.stonks.manager.ConfigManager;
import fr.lezoo.stonks.manager.PlayerDataManager;
import fr.lezoo.stonks.manager.QuotationManager;
import fr.lezoo.stonks.version.ServerVersion;
import fr.lezoo.stonks.version.SpigotPlugin;
import fr.lezoo.stonks.version.wrapper.VersionWrapper;
import fr.lezoo.stonks.version.wrapper.VersionWrapper_1_17_R1;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public ServerVersion version;
    public final ConfigManager configManager = new ConfigManager();
    public Economy economy;

    // TODO fixer l'initialisation de ces classes
    public VersionWrapper versionWrapper = new VersionWrapper_1_17_R1();
    public PlayerDataManager playerManager = new PlayerDataManager();
    public QuotationManager quotationManager = new QuotationManager();

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

        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(online -> playerManager.setup(online));

        // Reload config BEFORE config is reloaded
        saveDefaultConfig();

        // Initialize managers
        configManager.reload();

        // PlaceholderAPI compatibility
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            new StonksPlaceholders().register();
            getLogger().log(Level.INFO, "Hooked onto PlaceholderAPI");
        }

        // Register test quotation
        List<QuotationInfo> quot = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            quot.add(new QuotationInfo(System.currentTimeMillis() + 10000 * i, Math.log(1 + i)));
        Quotation quotation = new Quotation("aaa", "ooo", "hiiii", quot);
        quotationManager.register(quotation);

        // Register commands
        getCommand("stonks").setExecutor(new StonksCommand());
        getCommand("stonks").setTabCompleter(new StonksCommandCompletion());

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }
}
