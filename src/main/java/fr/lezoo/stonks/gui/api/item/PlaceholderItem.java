package fr.lezoo.stonks.gui.api.item;

import fr.lezoo.stonks.gui.api.GeneratedInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class PlaceholderItem<T extends GeneratedInventory> extends InventoryItem<T> {
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int modelData;
    private final boolean hideFlags;

    public PlaceholderItem(ConfigurationSection config) {
        this(Material.valueOf(config.getString("item", "").toUpperCase().replace(" ", "_").replace("-", "_")), config);
    }

    public PlaceholderItem(Material material, ConfigurationSection config) {
        super(config);

        this.material = material;
        this.name = config.getString("name");
        this.lore = config.getStringList("lore");
        this.hideFlags = config.getBoolean("hide-flags");
        this.modelData = config.getInt("model-data");
    }

    public Material getMaterial() {
        return material;
    }

    public boolean hideFlags() {
        return hideFlags;
    }

    public boolean hasName() {
        return name != null;
    }

    public String getName() {
        return name;
    }

    public boolean hasLore() {
        return lore != null && !lore.isEmpty();
    }

    public List<String> getLore() {
        return lore;
    }

    public int getModelData() {
        return modelData;
    }

    @Override
    public boolean isDisplayed(T inv) {
        return true;
    }

    public abstract Placeholders getPlaceholders(T inv, int n);

    @Override
    public ItemStack getDisplayedItem(T inv, int n) {

        Placeholders placeholders = getPlaceholders(inv, n);
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (hasName())
            meta.setDisplayName(placeholders.apply(inv.getPlayer(), getName()));

        if (hideFlags())
            meta.addItemFlags(ItemFlag.values());

        if (hasLore()) {
            List<String> lore = new ArrayList<>();
            getLore().forEach(line -> lore.add(ChatColor.GRAY + placeholders.apply(inv.getPlayer(), line)));
            meta.setLore(lore);
        }

        meta.setCustomModelData(getModelData());

        item.setItemMeta(meta);
        return item;
    }
}
