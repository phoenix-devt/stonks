package fr.lezoo.stonks.gui.api.item;

import fr.lezoo.stonks.gui.api.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;

/**
 * An inventory item that has no particular placeholder
 * yet it DOES support PAPI placeholders.
 */
public class SimpleItem<T extends GeneratedInventory> extends InventoryItem<T> {
    public SimpleItem(ConfigurationSection config) {
        super(config);
    }

    @Override
    public Placeholders getPlaceholders(T inv, int n) {
        return new Placeholders();
    }
}
