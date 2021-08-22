package fr.lezoo.stonks.item;

import fr.lezoo.stonks.gui.objects.item.Placeholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TradingBook extends CustomItem<Void> {

    public TradingBook(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void whenBuilt(ItemStack item, ItemMeta meta, Void v) {
        // Nothing there
    }

    @Override
    public Placeholders getPlaceholders(Player player, Void v) {
        return new Placeholders();
    }
}
