package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.ShareClosedEvent;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.share.CloseReason;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class ShareManager {
    private final Map<UUID, Share> mapped = new HashMap<>();

    public void refresh() {
        for (Share share : mapped.values())
            if (share.isOpen()) {

                // Check if the share needs to be closed
                if (share.getMaxPrice() <= share.getStock().getPrice() || share.getMinPrice() >= share.getStock().getPrice()) {
                    // Close the share
                    share.close(CloseReason.AUTOMATIC);
                    Bukkit.getPluginManager().callEvent(new ShareClosedEvent(share));
                    return;
                }

                // Automatically closes share when in deficit
                if (share.getCloseEarning(0) <= 0) {
                    share.close(CloseReason.DEFICIT);
                    Bukkit.getPluginManager().callEvent(new ShareClosedEvent(share));
                    return;
                }
            }
    }

    public void load() {

        // Register shares
        FileConfiguration config = new ConfigFile("share-data").getConfig();
        for (String key : config.getKeys(false))
            try {
                Share share = new Share(config.getConfigurationSection(key));
                mapped.put(share.getUniqueId(), share);
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load share '" + key + "': " + exception.getMessage());
            }
    }

    public void save() {
        ConfigFile config = new ConfigFile("share-data");

        // Remove old
        for (String key : config.getConfig().getKeys(false))
            config.getConfig().set(key, null);

        // Save newest
        for (Share share : mapped.values())
            share.saveInConfig(config.getConfig());

        config.save();
    }

    public boolean has(UUID uuid) {
        return mapped.containsKey(uuid);
    }

    public Collection<Share> getShares() {
        return mapped.values();
    }

    /**
     * @return All shares from that stock
     */
    public Set<Share> getByStock(Stock stock) {
        Set<Share> shares = new HashSet<>();

        for (Share checked : mapped.values())
            if (checked.getStock().equals(stock))
                shares.add(checked);

        return shares;
    }

    /**
     * Registers a player share in the share registry. This
     * does NOT add the share to the player's mapped shares list
     * which must be done using {@link PlayerData#giveShare(Share)}
     *
     * @param share Share to register
     */
    public void register(Share share) {
        Validate.isTrue(!mapped.containsKey(share.getUniqueId()), "Cannot register two shares with the same ID");

        mapped.put(share.getUniqueId(), share);
    }

    /**
     * Unregisters a player share from the share registry. This
     * does NOT remove the share from the {@link PlayerData}'s
     * mapped shares list. This must be done using {@link PlayerData#unregisterShare(Share)}
     *
     * @param share Share to unregister
     */
    public void unregister(Share share) {
        mapped.remove(share.getUniqueId());
    }

    public Share get(UUID uuid) {
        return mapped.get(uuid);
    }
}
