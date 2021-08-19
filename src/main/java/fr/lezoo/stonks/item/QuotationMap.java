package fr.lezoo.stonks.item;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.map.QuotationMapRenderer;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationTimeDisplay;
import fr.lezoo.stonks.display.DisplayInfo;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.text.DecimalFormat;

public class QuotationMap extends CustomItem<DisplayInfo> {

    public QuotationMap(ConfigurationSection config) {
        super(config);
    }

    //We add the mapRenderer
    @Override
    public ItemStack build(Player player, DisplayInfo info) {
        Quotation quotation = info.getQuotation();
        QuotationTimeDisplay time = info.getTimeDisplay();

        ItemStack item = super.build(player, info);
        MapMeta meta = (MapMeta) item.getItemMeta();
        MapView mapView = meta.getMapView();
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(quotation, time));
        item.setItemMeta(meta);
        return item;

    }

    @Override
    public Placeholders getPlaceholders(Player player, DisplayInfo info) {
        Quotation quotation = info.getQuotation();
        QuotationTimeDisplay timeDisplay = info.getTimeDisplay();
        DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;
        Placeholders holders = new Placeholders();
        holders.register("quotation-id", quotation.getId());
        holders.register("company", quotation.getName());
        holders.register("current-price", format.format(quotation.getPrice()));
        holders.register("lowest-price", format.format(quotation.getLowest(timeDisplay)));
        holders.register("highest-price", format.format(quotation.getHighest(timeDisplay)));
        holders.register("evolution", quotation.getEvolution(timeDisplay));
        holders.register("time-visualized", timeDisplay.toString().toLowerCase());
        holders.register("quotation-type", quotation.getClass().getName());
        return holders;

    }

    @Override
    public void whenBuilt(ItemStack item, ItemMeta meta, DisplayInfo info) {

    }
}
