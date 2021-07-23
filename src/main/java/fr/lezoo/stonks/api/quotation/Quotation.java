package fr.lezoo.stonks.api.quotation;


import fr.lezoo.stonks.api.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.util.ArrayList;
import java.util.List;

/**
 * Place where players can buy and sell shares
 */
public class Quotation {
    private final String id, companyName, stockName;
    private  List<QuotationInfo> quotationData = new ArrayList<>();

    public Quotation(String id, String companyName, String stockName,List<QuotationInfo> quotationData) {
        this.id = id;
        this.companyName = companyName;
        this.stockName = stockName;
        this.quotationData=quotationData;
    }

    private double price;

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

    public double getPrice() {
        return price;
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
     * This method compares the current price with the info which time
     * stamp matches the most the time stamp given as parameter
     * <p>
     * TODO use linear interpolation instead
     *
     * @param delta Difference of time in the past, in millis
     * @return Growth rate compared to some time ago
     */
    public double getEvolution(long delta) {
        QuotationInfo info = getClosestInfo(System.currentTimeMillis() - delta);

        // Check if no division by zero?
        double growthRate = 100 * Math.abs((getPrice() - info.getPrice()) / info.getPrice());

        return Utils.truncate(growthRate, 1);
    }

    /**
     * @return Quotation info with closest time
     * stamp to the one given as parameter
     */
    private QuotationInfo getClosestInfo(long timeStamp) {
        Validate.isTrue(!quotationData.isEmpty(), "Quotation data is empty");

        QuotationInfo closest = null;
        long delta = Long.MAX_VALUE;

        for (QuotationInfo info : quotationData)
            if (Math.abs(info.getTimeStamp() - timeStamp) < delta) {
                delta = Math.abs(info.getTimeStamp() - timeStamp);
                closest = info;
            }

        return closest;
    }

    /**
     * @param NUMBER_DATA number of points taken for the graphic
     * @return a map where we can see the quotation
     */


    public ItemStack createQuotationMap(int NUMBER_DATA) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
        //We cast the ItemMeta into MapMeta
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED+"StockPaper of :"+companyName);


        //Creates a mapview to later change its Renderer and load img
        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(quotationData, NUMBER_DATA));
        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);
        return mapItem;

    }
}
