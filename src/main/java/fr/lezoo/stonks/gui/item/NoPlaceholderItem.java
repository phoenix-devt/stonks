package fr.lezoo.stonks.gui.item;

import fr.lezoo.stonks.gui.PluginInventory;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

public class NoPlaceholderItem extends InventoryPlaceholderItem {
	public NoPlaceholderItem(ConfigurationSection config) {
		super(config);
	}

	public NoPlaceholderItem(Material material, ConfigurationSection config) {
		super(material, config);
	}

	@Override
	public Placeholders getPlaceholders(PluginInventory inv, int n) {
		return new Placeholders();
	}
}
