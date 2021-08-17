package fr.lezoo.stonks.item;

import fr.lezoo.stonks.gui.api.item.Placeholders;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class TradingBook extends CustomItem<Player>{

    public TradingBook(ConfigurationSection config) {
        super(config);
    }

    @Override
    public Placeholders getPlaceholders(Player player, Player player2) {
        return new Placeholders();
    }

    @Override
    public void whenBuilt(ItemStack item, ItemMeta meta, Player player) {

    }
}
