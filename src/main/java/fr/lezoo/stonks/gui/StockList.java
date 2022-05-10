package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.EditableInventory;
import fr.lezoo.stonks.gui.objects.GeneratedInventory;
import fr.lezoo.stonks.gui.objects.item.InventoryItem;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.gui.objects.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.TimeScale;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of all stocks, when clicking one
 * you get the menu where you can buy or sell shares
 * using the custom {@link ShareMenu} GUI
 */
public class StockList extends EditableInventory {
    public StockList() {
        super("stock-list");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.equalsIgnoreCase("stock"))
            return new StockItem(config);

        if (function.equalsIgnoreCase("next-page"))
            return new NextPageItem(config);

        if (function.equalsIgnoreCase("previous-page"))
            return new PreviousPageItem(config);

        return new SimpleItem(config);
    }

    public GeneratedInventory generate(PlayerData player) {
        return new GeneratedStockList(player, this);
    }

    public class GeneratedStockList extends GeneratedInventory {
        private final List<Stock> stocks = new ArrayList<>();
        private final int maxPage;

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        public GeneratedStockList(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            int perPage = editable.getByFunction("stock").getSlots().size();

            stocks.addAll(Stonks.plugin.stockManager.getStocks());
            maxPage = Math.max(((int) Math.ceil((double) stocks.size() / perPage)) - 1, 0);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{page}", "" + (page + 1)).replace("{max}", "" + (maxPage + 1));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Market is closing!
            if (Stonks.plugin.isClosed()) {
                Message.MARKET_CLOSING.format().send(player);
                player.closeInventory();
                return;
            }

            // Next Page
            if (item instanceof NextPageItem && page < maxPage) {
                page++;
                open();
                return;
            }

            // Previous Page
            if (item instanceof PreviousPageItem && page > 0) {
                page--;
                open();
                return;
            }

            if (item instanceof StockItem) {
                ItemStack itemStack = event.getCurrentItem();
                PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
                String stockId = container.get(new NamespacedKey(Stonks.plugin, "stock_id"), PersistentDataType.STRING);
                if (stockId == null || stockId.isEmpty())
                    return;

                Stock stock = Stonks.plugin.stockManager.get(stockId);
                if (event.getAction() == InventoryAction.PICKUP_ALL)
                    Stonks.plugin.configManager.QUOTATION_SHARE.generate(playerData, stock).open();
                if (event.getAction() == InventoryAction.PICKUP_HALF)
                    Stonks.plugin.configManager.SPECIFIC_PORTFOLIO.generate(playerData, stock).open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class NextPageItem extends SimpleItem<GeneratedStockList> {
        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedStockList inv) {
            return inv.page < inv.maxPage;
        }
    }

    public class PreviousPageItem extends SimpleItem<GeneratedStockList> {
        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedStockList inv) {
            return inv.page > 0;
        }
    }

    public class StockItem extends InventoryItem<GeneratedStockList> {
        private final InventoryItem none, physicalStock;

        public StockItem(ConfigurationSection config) {
            super(config);

            none = new SimpleItem(config.getConfigurationSection("none"));
            physicalStock = new SubStockItem(config.getConfigurationSection("physical"), this);
        }

        @Override
        public ItemStack getDisplayedItem(GeneratedStockList inv, int n) {
            int index = getSlots().size() * inv.page + n;

            // If above stock number, display 'No stock'
            if (index >= inv.stocks.size())
                return none.getDisplayedItem(inv, n);

            Stock stock = inv.stocks.get(index);

            // Displayed required stock
            ItemStack displayed = stock.isVirtual() ? super.getDisplayedItem(inv, n) : physicalStock.getDisplayedItem(inv, n);
            ItemMeta meta = displayed.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Stonks.plugin, "stock_id"), PersistentDataType.STRING, stock.getId());
            displayed.setItemMeta(meta);

            return displayed;
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(GeneratedStockList inv, int n) {
            int index = getSlots().size() * inv.page + n;
            Stock stock = inv.stocks.get(index);

            Placeholders holders = new Placeholders();

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("name", stock.getName());
            holders.register("price", format.format(stock.getPrice()));
            holders.register("day-low", format.format(stock.getLowest(TimeScale.DAY)));
            holders.register("day-high", format.format(stock.getHighest(TimeScale.DAY)));
            holders.register("week-low", format.format(stock.getLowest(TimeScale.WEEK)));
            holders.register("week-high", format.format(stock.getHighest(TimeScale.WEEK)));
            holders.register("month-low", format.format(stock.getLowest(TimeScale.MONTH)));
            holders.register("month-high", format.format(stock.getHighest(TimeScale.MONTH)));
            holders.register("exchange-type", stock.isVirtual() ? "money" : stock.getExchangeType().toString().toLowerCase());
            holders.register("stock-type", stock.getClass().getSimpleName());
            return holders;
        }

        public class SubStockItem extends InventoryItem<GeneratedStockList> {
            private final InventoryItem parent;

            public SubStockItem(ConfigurationSection config, InventoryItem parent) {
                super(config);

                this.parent = parent;
            }

            @Override
            public Placeholders getPlaceholders(GeneratedStockList inv, int n) {
                return parent.getPlaceholders(inv, n);
            }
        }
    }
}
