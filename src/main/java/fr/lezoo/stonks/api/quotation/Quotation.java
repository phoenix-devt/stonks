package fr.lezoo.stonks.api.quotation;


import fr.lezoo.stonks.api.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.spigotmc.Metrics;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Place where players can buy and sell shares
 */
public class Quotation {
    private final String id, companyName, stockName;
    private List<QuotationInfo> quotationData = new ArrayList<>();
    //Refresh time of the quotation in milliseconds
    private final static int REFRESH_TIME = 1000;

    public Quotation(String id, String companyName, String stockName, List<QuotationInfo> quotationData) {
        this.id = id;
        this.companyName = companyName;
        this.stockName = stockName;
        this.quotationData = quotationData;
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

    public List<QuotationInfo> getQuotationData() {
        return quotationData;
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


    public BufferedImage getQuotationBoardImage(int NUMBER_DATA) {
        //Number of pixel in one line in the image
        final double IMAGE_SIZE = 128 * 5;
        //If not enough data on quotation data we take care of avoiding IndexOutOfBounds
        BufferedImage image = new BufferedImage((int) IMAGE_SIZE, (int) IMAGE_SIZE, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        int data_taken = Math.min(NUMBER_DATA, quotationData.size());
        int index = quotationData.size() - data_taken;
        //We look at the lowest val in the time we look backward to set the scale
        double minVal = getLowest(NUMBER_DATA * REFRESH_TIME);
        double maxVal = getHighest(NUMBER_DATA * REFRESH_TIME);
        g2d.setColor(new Color(126, 51, 0));
        g2d.fill(new Rectangle2D.Double(0, 0.2 * IMAGE_SIZE, IMAGE_SIZE, 0.8 * IMAGE_SIZE));
        g2d.setColor(Color.RED);
        Path2D.Double curve = new Path2D.Double();


        //If price = maxVal y =0.2 IMAGE_SIZE
        //If price = min Val y=IMAGE_SIZE (BOTTOM)
        double x= 0;
        double y=IMAGE_SIZE-(0.8*IMAGE_SIZE*(quotationData.get(index).getPrice()-minVal)/(maxVal-minVal));
        curve.moveTo(x,y);
        for (int i = 1; i < data_taken; i++) {
            //if data_taken < NUMBER_DATA,the graphics will be on the left of the screen mainly
            x=(double)i*IMAGE_SIZE/NUMBER_DATA;
            y=IMAGE_SIZE-(0.8*IMAGE_SIZE*(quotationData.get(index+i).getPrice()-minVal)/(maxVal-minVal));
            curve.lineTo(x,y);
        }
        g2d.draw(curve);
        return image;
    }


    /**
     * Creates a 5x5 map of the Quotation to the player
     * gives the player all the maps in his inventory
     */
    public void createQuotationBoard(Player player, int NUMBER_DATA) {
        BufferedImage image = getQuotationBoardImage(NUMBER_DATA);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                ItemStack item = new ItemStack(Material.FILLED_MAP, 1);
                MapMeta meta = (MapMeta) item.getItemMeta();
                MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
                mapView.getRenderers().clear();
                mapView.setTrackingPosition(false);
                //We draw on ea
                mapView.addRenderer(new QuotationBoardRenderer(image.getSubimage(128 * i, 128 * j, 128, 128)));
                meta.setMapView(mapView);
                item.setItemMeta(meta);
                player.getInventory().addItem(item);

            }
        }


    }


    /**
     * @param NUMBER_DATA number of points taken for the graphic
     * @return a map where we can see the quotation
     */

    public ItemStack createQuotationMap(int NUMBER_DATA) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
        //We cast the ItemMeta into MapMeta
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "StockPaper of :" + companyName);
        meta.setLore(Arrays.asList(ChatColor.BLUE + "Company name : " + companyName, ChatColor.GREEN + "Stock name : " + stockName));

        //Creates a mapview to later change its Renderer and load img
        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(this, NUMBER_DATA));
        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);
        return mapItem;

    }
}
