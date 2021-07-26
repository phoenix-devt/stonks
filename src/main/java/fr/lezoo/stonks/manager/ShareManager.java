package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.share.Share;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ShareManager {
    private final Map<UUID, Share> mapped = new HashMap<>();

    public void load() {

        // Register shares
        FileConfiguration config = new ConfigFile("sharedata").getConfig();
        for (String key : config.getKeys(false))
            try {
                Share share = new Share(config.getConfigurationSection(key));
                mapped.put(share.getUniqueId(), share);
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load share '" + key + "': " + exception.getMessage());
            }
    }

    public void save() {
        ConfigFile config = new ConfigFile("sharedata");

        // Remove old
        for (String key : config.getConfig().getKeys(false))
            config.getConfig().set(key, null);

        // Save newest
        for (Share share : mapped.values())
            share.saveInConfig(config.getConfig());

        config.save();
    }

    public void register(Share share) {
        Validate.isTrue(!mapped.containsKey(share.getUniqueId()), "Cannot register two shares with the same ID");

        mapped.put(share.getUniqueId(), share);
    }

    public boolean has(UUID uuid) {
        return mapped.containsKey(uuid);
    }

    public Share get(UUID uuid) {
        return mapped.get(uuid);
    }
}
