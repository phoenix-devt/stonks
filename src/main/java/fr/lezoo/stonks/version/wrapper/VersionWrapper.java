package fr.lezoo.stonks.version.wrapper;

import fr.lezoo.stonks.api.NBTItem;
import org.bukkit.inventory.ItemStack;

public interface VersionWrapper {
	public NBTItem getNBTItem(ItemStack item);
}
