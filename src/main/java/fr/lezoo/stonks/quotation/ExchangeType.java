package fr.lezoo.stonks.quotation;

import org.bukkit.Material;

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

    public ExchangeType(Material material) {
        this(material, 0);
    }

    public Material getMaterial() {
        return material;
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
