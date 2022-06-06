package fr.lezoo.stonks.stock;

import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ExchangeType {
    private final Material material;
    private final int modelData;

    public ExchangeType(ConfigurationSection config) {
        material = Material.valueOf(Utils.enumName(config.getString("material")));
        Validate.isTrue(material != Material.AIR, "Cannot use AIR as exchange type");
        modelData = config.getInt("model-data");
    }

    public Material getMaterial() {
        return material;
    }

    public boolean hasModelData() {
        return modelData > 0;
    }

    public int getModelData() {
        return modelData;
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
