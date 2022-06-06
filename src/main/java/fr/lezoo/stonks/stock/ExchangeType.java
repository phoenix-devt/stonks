package fr.lezoo.stonks.stock;

import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ExchangeType {
    private final Material material;
    private final int modelData;
    private final String display;

    public ExchangeType(ConfigurationSection config) {
        material = Material.valueOf(Utils.enumName(config.getString("material")));
        Validate.isTrue(material != Material.AIR, "Cannot use AIR as exchange type");
        modelData = config.getInt("model-data");
        display = config.getString("display", Utils.caseOnWords(material.name().toLowerCase().replace("_", " ")));
    }

    public Material getMaterial() {
        return material;
    }

    public String getDisplay() {
        return display;
    }

    public boolean hasModelData() {
        return modelData > 0;
    }

    public int getModelData() {
        return modelData;
    }

    public ItemStack generateItem() {
        ItemStack stack = new ItemStack(material);
        if (hasModelData()) {
            ItemMeta meta = stack.getItemMeta();
            meta.setCustomModelData(modelData);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    public boolean matches(@Nullable ItemStack item) {
        if (item == null || item.getType() == Material.AIR)
            return false;

        return item.getType() == material && (item.hasItemMeta() ? item.getItemMeta().getCustomModelData() : 0) == modelData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExchangeType that = (ExchangeType) o;
        return modelData == that.modelData && material == that.material;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, modelData);
    }

    @Override
    public String toString() {
        return "ExchangeType{" +
                "material=" + material +
                ", modelData=" + modelData +
                '}';
    }
}
