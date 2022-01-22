package fr.lezoo.stonks.quotation;

import org.bukkit.Material;

public class ExchangeType {
    private final Material material;
    private final int modelData;

    /**
     *
     * @param material of the item exchanged
     * @param modelData modelData of the item
     */
    public ExchangeType(Material material, int modelData) {
        this.material = material;
        this.modelData = modelData;
    }
    public ExchangeType(Material material) {
        this(material,0);
    }

    public Material getMaterial() {
        return material;
    }

    public int getModelData() {
        return modelData;
    }

    public boolean equals(ExchangeType exchangeType) {
        return material.equals(exchangeType.getMaterial())&&exchangeType.getModelData()==modelData;
    }

    @Override
    public String toString(){
        if(modelData==0)
            return material.name().toLowerCase();
        else
            return material.name().toLowerCase()+" "+modelData;
    }
}
