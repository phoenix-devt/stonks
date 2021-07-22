package fr.lezoo.stonks;

import fr.lezoo.stonks.comp.placeholder.PlaceholderParser;
import fr.lezoo.stonks.manager.PlayerDataManager;
import fr.lezoo.stonks.version.ServerVersion;
import fr.lezoo.stonks.version.wrapper.VersionWrapper;
import org.bukkit.plugin.java.JavaPlugin;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public PlaceholderParser placeholderParser;
    public VersionWrapper versionWrapper;
    public ServerVersion version;
    public PlayerDataManager playerManager;

    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {

        // Change once plugin is posted on Spigot
        /*new Metrics(this, 111111);*/


    }
}
