package fr.lezoo.stonks.api.quotation;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Place where players can buy and sell stocks
 */
public abstract class Quotation {
    private final String id, companyName, stockName;
    private final List<QuotationInfo> quotationData = new ArrayList<>();

    public Quotation(String id, String companyName, String stockName) {
        this.id = id;
        this.companyName = companyName;
        this.stockName = stockName;
    }

    public String getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getStockName() {
        return stockName;
    }

    /**
     * @param timeOut The delay in the past in millis on which the lowest value
     *                is calculated. For instance, if set to 1000 * 60 * 60 * 24,
     *                lowest value will be calculated over the last day.
     *                <p>
     *                For instance, QuotationInfo.WEEK_TIME_OUT or MONTH_TIME_OUT
     * @return Lowest value for the last X millis
     */
    public double getLowest(long timeOut) {
        double lowest = Double.MAX_VALUE;

        // Iterate through the list revert
        int k = quotationData.size() - 1;
        while (k >= 0) {
            QuotationInfo data = quotationData.get(k);
            if (System.currentTimeMillis() - data.getTimeStamp() > timeOut)
                break;

            if (data.getPrice() < lowest)
                lowest = data.getPrice();
            k--;
        }

        return lowest;
    }

    /**
     * @param timeOut The delay in the past in millis on which the highest value
     *                is calculated. For instance, if set to 1000 * 60 * 60 * 24,
     *                highest value will be calculated over the last day.
     *                <p>
     *                For instance, QuotationInfo.WEEK_TIME_OUT or MONTH_TIME_OUT
     * @return Highest value for the last X millis
     */
    public double getHighest(long timeOut) {
        double highest = Double.MIN_VALUE;

        // Iterate through the list revert
        int k = quotationData.size() - 1;
        while (k >= 0) {
            QuotationInfo data = quotationData.get(k);
            if (System.currentTimeMillis() - data.getTimeStamp() > timeOut)
                break;

            if (data.getPrice() > highest)
                highest = data.getPrice();
            k--;
        }

        return highest;
    }


    /**
     * @return a map where we can see the quotation
     */
    public ItemStack createQuotationMap() {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
        //We cast the ItemMeta into MapMeta
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        //Creates a mapview to later change its Renderer and load img
        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(quotationData));

        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);
        return mapItem;

    }

}
