package fr.lezoo.stonks.gui.api;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;

public abstract class EditableInventory {
    private final String id;

    private String name;
    private int slots;

    /**
     * This set is linked so it keeps the order/priority
     * in which the items are loaded from the config
     */
    private final Set<InventoryItem> items = new LinkedHashSet<>();

    public EditableInventory(String id) {
        Validate.notNull(this.id = id, "ID must not be null");
    }

    public void reload(ConfigurationSection config) {

        this.name = config.getString("name");
        Validate.notNull(name, "Name must not be null");

        this.slots = Math.min(Math.max(9, config.getInt("slots")), 54);
        Validate.isTrue((slots % 9) == 0, "Slots must be a multiple of 9");

        items.clear();
        if (config.contains("items")) {
            Validate.notNull(config.getConfigurationSection("items"), "Could not load item list");
            for (String key : config.getConfigurationSection("items").getKeys(false))
                try {
                    ConfigurationSection section = config.getConfigurationSection("items." + key);
                    Validate.notNull(section, "Could not load config");
                    items.add(loadInventoryItem(section));
                } catch (IllegalArgumentException exception) {
                    Stonks.plugin.getLogger().log(Level.WARNING, "Could not load item '" + key + "' from inventory '" + getId() + "': " + exception.getMessage());
                }
        }
    }

    public String getId() {
        return id;
    }

    public Set<InventoryItem> getItems() {
        return items;
    }

    public String getName() {
        return name;
    }

    public int getSlots() {
        return slots;
    }

    public InventoryItem getByFunction(String function) {
        for (InventoryItem item : items)
            if (item.getFunction().equals(function))
                return item;
        return null;
    }

    /**
     * Method used to load an item in the custom inventory
     *
     * @param function The item function
     * @param config   The configuration section to load the item from
     * @return Loaded inventory item
     */
    public abstract InventoryItem load(String function, ConfigurationSection config);

    private InventoryItem loadInventoryItem(ConfigurationSection config) {
        String function = config.contains("function") ? config.getString("function").toLowerCase() : "";

        /*if (function.startsWith("trigger:"))
            // Load trigger item*/

        return load(function, config);
    }
}
