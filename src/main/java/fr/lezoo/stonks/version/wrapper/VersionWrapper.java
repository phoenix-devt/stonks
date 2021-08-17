package fr.lezoo.stonks.version.wrapper;

import fr.lezoo.stonks.version.NBTItem;
import org.bukkit.inventory.ItemStack;

/**
 * Interface to handle code functions that depend on the server version.
 *
 * @author Jules
 */
public interface VersionWrapper {
    public NBTItem getNBTItem(ItemStack item);
}
