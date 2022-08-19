package fr.lezoo.stonks.item;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.DisplayInfo;
import fr.lezoo.stonks.display.map.StockMapRenderer;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.TimeScale;
import fr.lezoo.stonks.stock.handler.RealStockHandler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;

import java.text.DecimalFormat;

public class StockMap extends CustomItem<DisplayInfo> {

    public StockMap(ConfigurationSection config) {
        super(config);
    }

    //We add the mapRenderer
    @Override
    public ItemStack build(Player player, DisplayInfo info) {
        Stock stock = info.getStock();
        TimeScale time = info.getTimeDisplay();

        ItemStack item = super.build(player, info);
        MapMeta meta = (MapMeta) item.getItemMeta();
        //We create a mpa view
        MapView mapView = Bukkit.createMap(player.getWorld());
        mapView.getRenderers().clear();
        mapView.addRenderer(new StockMapRenderer(player, item, stock, time));
        mapView.setUnlimitedTracking(false);
        mapView.setTrackingPosition(false);
        meta.setMapView(mapView);
        item.setItemMeta(meta);
        return item;

    }

    @Override
    public Placeholders getPlaceholders(Player player, DisplayInfo info) {
        Stock stock = info.getStock();
        TimeScale timeDisplay = info.getTimeDisplay();
        DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;
        Placeholders holders = new Placeholders();
        holders.register("stock-id", stock.getId());
        holders.register("stock-name", stock.getName());
        holders.register("current-price", format.format(stock.getPrice()));
        holders.register("lowest-price", format.format(stock.getLowest(timeDisplay)));
        holders.register("highest-price", format.format(stock.getHighest(timeDisplay)));
        holders.register("evolution", stock.getEvolution(timeDisplay));
        holders.register("time-scale", timeDisplay.toString().toLowerCase());
        holders.register("stock-type", stock.getHandler() instanceof RealStockHandler ? "Real Stock" : "Virtual");
        return holders;

    }

    @Override
    public void whenBuilt(ItemStack item, ItemMeta meta, DisplayInfo info) {

    }
}
