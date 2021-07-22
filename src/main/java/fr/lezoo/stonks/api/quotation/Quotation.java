package fr.lezoo.stonks.api.quotation;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

/**
 * Place where players can buy and sell stocks
 */
public abstract class Quotation {
    private final String id;
    private QuotationInfo[] quotationData;

    public Quotation(String id) {
        this.id = id;
    }


    /**
     *
     * @return a map where we can see the quotation
     */
    public ItemStack createQuotationMap() {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP,1);
        //We cast the ItemMeta into MapMeta
        MapMeta meta = (MapMeta)mapItem.getItemMeta();
        //Creates a mapview to later change its Renderer and load img
        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(quotationData));

        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);
        return mapItem;

    }

}
