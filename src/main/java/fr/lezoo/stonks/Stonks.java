package fr.lezoo.stonks;

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
import fr.lezoo.stonks.version.wrapper.VersionWrapper;
import fr.lezoo.stonks.version.wrapper.VersionWrapper_1_17_R1;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public ServerVersion version;
    public final ConfigManager configManager = new ConfigManager();

    // TODO fixer l'initialisation de ces classes
    public VersionWrapper versionWrapper = new VersionWrapper_1_17_R1();
    public PlayerDataManager playerManager = new PlayerDataManager();
    public QuotationManager quotationManager = new QuotationManager();

    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {
        /* TEST*/


        // Read server version
        try {
            version = new ServerVersion(Bukkit.getServer().getClass());
            getLogger().log(Level.INFO, "Detected Bukkit Version: " + version.toString());
        } catch (Exception exception) {
            getLogger().log(Level.INFO, ChatColor.RED + "Your server version is not compatible.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Change once plugin is posted on Spigot
        /*new Metrics(this, 111111);*/

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

        // Register commands
        getCommand("stonks").setExecutor(new StonksCommand());
        getCommand("stonks").setTabCompleter(new StonksCommandCompletion());

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }
}
