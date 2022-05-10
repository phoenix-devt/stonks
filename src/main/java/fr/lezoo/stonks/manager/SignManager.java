package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.sign.DisplaySign;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.util.Position;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;
import java.util.logging.Level;

public class SignManager implements FileManager {
    private final Map<Position, DisplaySign> mapped = new HashMap<>();

    public boolean has(Position position) {
        return mapped.containsKey(position);
    }

    public DisplaySign get(Position position) {
        return mapped.get(position);
    }

    /**
     * Update the information given by all the signs
     */
    public void refreshSigns() {
        for (DisplaySign sign : Stonks.plugin.signManager.getActive())
            sign.update();
    }

    public Collection<DisplaySign> getActive() {
        return mapped.values();
    }

    public void register(DisplaySign sign) {
        Validate.isTrue(!mapped.containsKey(sign.getPosition()), "Cannot register two signs at the same position");

        mapped.put(sign.getPosition(), sign);
    }

    public void unregister(Position pos) {
        mapped.remove(pos);
    }


    /**
     * @return Current display signs linked to given stock
     */
    public Set<DisplaySign> getByStock(Stock stock) {
        Set<DisplaySign> signs = new HashSet<>();

        for (DisplaySign sign : mapped.values())
            if (sign.getStock().equals(stock))
                signs.add(sign);

        return signs;
    }

    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("sign-data").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new DisplaySign(config.getConfigurationSection(key)));
            } catch (RuntimeException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load display sign " + key + ": " + exception.getMessage());
            }
    }

    @Override
    public void save() {
        ConfigFile config = new ConfigFile("sign-data");

        // Remove older
        for (String key : config.getConfig().getKeys(false))
            config.getConfig().set(key, null);

        // Save latest
        for (DisplaySign sign : getActive())
            sign.save(config.getConfig());

        config.save();
    }
}
