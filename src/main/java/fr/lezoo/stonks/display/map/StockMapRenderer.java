package fr.lezoo.stonks.display.map;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.DisplayInfo;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.item.StockMap;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.TimeScale;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Used to render the stock evolution on a map item
 */
public class StockMapRenderer extends MapRenderer {
    //We keep track of the map and the player that posses it
    private final Player player;
    private final ItemStack map;

    private final Stock stock;
    private final int datataken;
    private List<StockInfo> stockData;
    private TimeScale time;
    private final int refreshRate = (int) (Stonks.plugin.configManager.mapRefreshTime * 20);
    private int counter = refreshRate;

    public StockMapRenderer(Player player, ItemStack map, Stock stock, TimeScale time) {
        this.player = player;
        this.map = map;
        this.stock = stock;
        stockData = stock.getData(time);
        this.time = time;
        // We take the min of the theoric DATA_NUMBER that we want and the real length size of stockData to avoid IndexOutOfBounds
        this.datataken = Math.min(stockData.size(), Stock.BOARD_DATA_NUMBER);
    }

    public BufferedImage getStockImage() {
        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();

        List<StockInfo> stockData = stock.getData(time);
        // If the stock is Empty we print an error
        Validate.isTrue(stockData.size() != 0, "Quotation '" + stock.getId() + "' has no data!");

        int data_taken = Math.min(Stock.BOARD_DATA_NUMBER, stockData.size());
        int index = stockData.size() - data_taken;

        // We look at the lowest val in the time we look backward to set the scale
        double minVal = stockData.get(index).getPrice();
        double maxVal = stockData.get(index).getPrice();
        for (int i = 1; i < data_taken; i++) {
            if (stockData.get(index + i).getPrice() > maxVal)
                maxVal = stockData.get(index + i).getPrice();
            if (stockData.get(index + i).getPrice() < minVal)
                minVal = stockData.get(index + i).getPrice();
        }
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(0, 0, 128, 128));

        g2d.setColor(Color.RED);
        Path2D.Double curve = new Path2D.Double();
        // If price = maxVal y =0.05 IMAGE_SIZE
        // If price = min Val y=0.95*IMAGE_SIZE (BOTTOM)
        double x = 5;
        double y = 0.95 * 128 - (0.9 * 128 * (stockData.get(index).getPrice() - minVal) / (maxVal - minVal));
        curve.moveTo(x, y);
        for (int i = 1; i < data_taken; i++) {
            // if data_taken < NUMBER_DATA,the graphics will be on the left of the screen mainly
            x = 5 + i * 128 / 0.95 / Stock.BOARD_DATA_NUMBER;
            y = 0.95 * 128 - (0.9 * 128 * (stockData.get(index + i).getPrice() - minVal) / (maxVal - minVal));
            curve.lineTo(x, y);
        }
        g2d.draw(curve);
        return image;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (counter++ >= refreshRate) {
            //We update the meta of the map in order to keep it relevant.
            updateMeta();
            BufferedImage image = getStockImage();
            //We resize to take less RAM
            image = MapPalette.resizeImage(image);
            //draw image on canvas
            mapCanvas.drawImage(0, 0, image);
            counter = 0;
        }
    }

    /**
     * Updates the item meta of the meta in order to keep it relevant
     */
    public void updateMeta() {
        StockMap stockMap = Stonks.plugin.configManager.stockMap;
        Placeholders placeholder = stockMap.getPlaceholders(player, new DisplayInfo(stock, time));

        ItemMeta meta = map.getItemMeta();
        meta.setDisplayName(placeholder.apply(stockMap.getDisplayName()));
        meta.setLore(stockMap.getLore().stream().map(str -> str = placeholder.apply(str)).collect(Collectors.toList()));
        map.setItemMeta(meta);
    }
}

