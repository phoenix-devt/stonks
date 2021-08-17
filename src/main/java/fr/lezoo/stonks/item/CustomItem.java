package fr.lezoo.stonks.item;

import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.util.Utils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomItem<T> {
    private final Material material;
    private final int modelData;
    private final List<String> lore;
    private final String displayName;

    public CustomItem(ConfigurationSection config) {
        this.material = Material.valueOf(Utils.enumName(config.getString("type")));
        this.displayName = config.getString("name");
        this.lore = config.getStringList("lore");
        this.modelData = config.getInt("model-data");
    }

    /**
     * Build an item using specific lore and name placeholders.
     *
     * @param player Player to parse placeholders from
     * @param t      Object to build the item stack onto
     * @return Built item
     */
    public ItemStack build(Player player, T t) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        Placeholders placeholders = getPlaceholders(player, t);

        // Apply custom model data
        if (modelData > 0)
            meta.setCustomModelData(modelData);

        // Apply display name
        if (displayName != null)
            meta.setDisplayName(placeholders.apply(player, displayName));

        // Apply lore
        if (lore != null && !lore.isEmpty()) {
            List<String> built = new ArrayList<>();
            for (String str : lore)
                built.add(placeholders.apply(player, str));
            meta.setLore(built);
        }

        // Apply special effects
        whenBuilt(item, meta, t);

        item.setItemMeta(meta);

        return item;
    }

    public abstract Placeholders getPlaceholders(Player player, T t);

    public abstract void whenBuilt(ItemStack item, ItemMeta meta, T t);
}
