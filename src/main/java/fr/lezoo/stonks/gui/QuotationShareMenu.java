package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.api.util.Utils;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.PlaceholderItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimplePlaceholderItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.text.DecimalFormat;

public class QuotationShareMenu extends EditableInventory {
    public QuotationShareMenu() {
        super("share-menu");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.startsWith("buy-custom"))
            // TODO
            return new SimplePlaceholderItem(config);

        if (function.startsWith("sell-custom"))
            // TODO
            return new SimplePlaceholderItem(config);

        if (function.startsWith("info"))
            return new QuotationInfoItem(config);

        if (function.startsWith("leverage"))
            return new LeverageItem(config);

        if (function.startsWith("buy"))
            return new BuyShareItem(function, config);

        if (function.startsWith("sell"))
            return new SimplePlaceholderItem(config);

        return new SimplePlaceholderItem(config);
    }

    public GeneratedInventory generate(PlayerData player, Quotation quotation) {
        return new GeneratedShareMenu(player, this, quotation);
    }

    public class GeneratedShareMenu extends GeneratedInventory {
        private final Quotation quotation;

        private double leverage = 1;

        public GeneratedShareMenu(PlayerData playerData, EditableInventory editable, Quotation quotation) {
            super(playerData, editable);

            this.quotation = quotation;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{company}", quotation.getCompanyName());
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            if (item.getFunction().equals("back")) {
                Stonks.plugin.configManager.QUOTATION_LIST.generate(playerData).open();
                return;
            }

            if (item instanceof BuyShareItem) {
                int amount = ((BuyShareItem) item).amount;
                double price = quotation.getPrice() * amount;

                // Check for balance




            }

            if (item instanceof QuotationInfoItem) {
                // TODO


            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class SellShareItem extends PlaceholderItem<GeneratedShareMenu> {
        private final int amount;

        public SellShareItem(String function, ConfigurationSection config) {
            super(config);

            this.amount = Integer.parseInt(function.substring(4));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.singleDigit.format(inv.leverage));
            holders.register("amount", amount);

            return holders;
        }
    }

    public class BuyShareItem extends PlaceholderItem<GeneratedShareMenu> {
        private final int amount;

        public BuyShareItem(String function, ConfigurationSection config) {
            super(config);

            this.amount = Integer.parseInt(function.substring(3));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.singleDigit.format(inv.leverage));
            holders.register("amount", amount);

            return holders;
        }
    }

    public class LeverageItem extends PlaceholderItem<GeneratedShareMenu> {
        public LeverageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("leverage", Utils.singleDigit.format(inv.leverage));

            return holders;
        }
    }

    public class QuotationInfoItem extends PlaceholderItem<GeneratedShareMenu> {
        public QuotationInfoItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("company-name", inv.quotation.getCompanyName());
            holders.register("stock-name", inv.quotation.getStockName());

            holders.register("week-low", format.format(inv.quotation.getLowest(QuotationInfo.WEEK_TIME_OUT)));
            holders.register("week-high", format.format(inv.quotation.getHighest(QuotationInfo.WEEK_TIME_OUT)));
            holders.register("month-low", format.format(inv.quotation.getLowest(QuotationInfo.MONTH_TIME_OUT)));
            holders.register("month-high", format.format(inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT)));

            // TODO instead of comparing to 1 day ago, compare to the beginning of the day, same with month, year..
            holders.register("hour-evolution", format.format(inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT)));
            holders.register("day-evolution", format.format(inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT)));
            holders.register("week-evolution", format.format(inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT)));
            holders.register("month-evolution", format.format(inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT)));

            return holders;
        }
    }
}
