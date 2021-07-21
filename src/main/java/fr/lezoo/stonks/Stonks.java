package fr.lezoo.stonks;

import fr.lezoo.stonks.comp.placeholder.PlaceholderParser;
import org.bukkit.plugin.java.JavaPlugin;

public class Stonks extends JavaPlugin {
    public static Stonks plugin;

    public PlaceholderParser placeholderParser;

    public void onLoad() {
        plugin = this;
    }

    public void onEnable() {

        // Change once plugin is posted on Spigot
        /*new Metrics(this, 111111);*/


    }
}
