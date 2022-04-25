package fr.lezoo.stonks.quotation;

import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

public class ExchangeType {
    private final Material material;
    private final int modelData;

    /**
     * @param material  Type of the item exchanged
     * @param modelData The item model data
     */
    public ExchangeType(Material material, int modelData) {
        this.material = material;
        this.modelData = modelData;
    }

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
