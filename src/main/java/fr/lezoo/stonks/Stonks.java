package fr.lezoo.stonks;

import fr.lezoo.stonks.command.StonksCommand;
import fr.lezoo.stonks.command.completion.StonksCommandCompletion;
import fr.lezoo.stonks.comp.placeholder.DefaultPlaceholderParser;
import fr.lezoo.stonks.comp.placeholder.PlaceholderAPIParser;
import fr.lezoo.stonks.comp.placeholder.PlaceholderParser;
import fr.lezoo.stonks.comp.placeholder.StonksPlaceholders;
import fr.lezoo.stonks.listener.PlayerListener;
import fr.lezoo.stonks.manager.PlayerDataManager;
import fr.lezoo.stonks.version.ServerVersion;
import fr.lezoo.stonks.version.wrapper.VersionWrapper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public PlaceholderParser placeholderParser = new DefaultPlaceholderParser();
    public VersionWrapper versionWrapper;
    public ServerVersion version;
    public PlayerDataManager playerManager;

    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {

        // Change once plugin is posted on Spigot
        /*new Metrics(this, 111111);*/

        // Initialize managers

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
