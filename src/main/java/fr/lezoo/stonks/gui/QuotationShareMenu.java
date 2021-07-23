package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.PlaceholderItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimplePlaceholderItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class QuotationShareMenu extends EditableInventory {
    public QuotationShareMenu() {
        super("share-menu");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.startsWith("leverage"))
            // TODO
            return null;

        if (function.startsWith("buy"))
            // TODO
            return null;

        if (function.startsWith("sell"))
            // TODO
            return null;

        return new SimplePlaceholderItem(config);
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

            if (item instanceof QuotationInfoItem) {
                // TODO


            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class BuyShareItem extends PlaceholderItem<GeneratedShareMenu> {
        private final int amount;

        public BuyShareItem(ConfigurationSection config, String function) {
            super(config);

            this.amount = Integer.parseInt(function.substring(3));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("price", inv.quotation.getPrice());
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

            holders.register("leverage", inv.leverage);

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

            holders.register("company-name", inv.quotation.getCompanyName());
            holders.register("stock-name", inv.quotation.getStockName());

            holders.register("week-low", inv.quotation.getLowest(QuotationInfo.WEEK_TIME_OUT));
            holders.register("week-high", inv.quotation.getHighest(QuotationInfo.WEEK_TIME_OUT));
            holders.register("month-low", inv.quotation.getLowest(QuotationInfo.MONTH_TIME_OUT));
            holders.register("month-high", inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT));

            // TODO instead of comparing to 1 day ago, compare to the beginning of the day, same with month, year..
            holders.register("hour-evolution", inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT));
            holders.register("day-evolution", inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT));
            holders.register("week-evolution", inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT));
            holders.register("month-evolution", inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT));

            return holders;
        }
    }
}
