package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemManager {
    public ItemStack createTradingBook() {

        ItemStack item = new ItemStack(Material.WRITABLE_BOOK,1);
        BookMeta meta = (BookMeta) item.getItemMeta();
        meta.setDisplayName(Stonks.plugin.configManager.tradingBookName);
        meta.setLore(Stonks.plugin.configManager.tradingBookLore);
        meta.setPages(Stonks.plugin.configManager.bookExplanationText);
        item.setItemMeta(meta);
        return item;

    }
}
