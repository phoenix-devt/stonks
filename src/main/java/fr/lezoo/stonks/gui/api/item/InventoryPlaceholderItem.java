package fr.lezoo.stonks.gui.api.item;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.PluginInventory;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public abstract class InventoryPlaceholderItem<T extends GeneratedInventory> extends InventoryItem<T> {
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int modelData;
    private final boolean hideFlags;

    public InventoryPlaceholderItem(ConfigurationSection config) {
        this(Material.valueOf(config.getString("item", "").toUpperCase().replace(" ", "_").replace("-", "_")), config);
    }

    public InventoryPlaceholderItem(Material material, ConfigurationSection config) {
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

    private void applyTexture(String value, SkullMeta meta) {
        try {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            profile.getProperties().put("textures", new Property("textures", value));

            Field profileField = meta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            profileField.set(meta, profile);
        } catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException exception) {
            Stonks.plugin.getLogger().log(Level.WARNING, "Could not apply item texture value of " + getId());
        }
    }
}
