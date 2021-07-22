package fr.lezoo.stonks.gui.api.item;

import fr.lezoo.stonks.gui.api.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;

/**
 * An inventory item that has no particular placeholder
 * yet it DOES support PAPI placeholders.
 */
public class SimplePlaceholderItem<T extends GeneratedInventory> extends PlaceholderItem<T> {
    public SimplePlaceholderItem(ConfigurationSection config) {
        super(config);
    }

    @Override
    public Placeholders getPlaceholders(T inv, int n) {
        return new Placeholders();
    }
}
