package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.NBTItem;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.PlaceholderItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimplePlaceholderItem;
import fr.lezoo.stonks.version.ItemTag;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuotationList extends EditableInventory {
    public QuotationList() {
        super("quotation-list");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equalsIgnoreCase("quotation"))
            return new QuotationItem(config);

        return new SimplePlaceholderItem(config);
    }

    public class GeneratedQuotationList extends GeneratedInventory {
        private final List<Quotation> quotations = new ArrayList<>();
        private final int maxPage;

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        public GeneratedQuotationList(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            int perPage = editable.getByFunction("quotation").getSlots().size();

            quotations.addAll(Stonks.plugin.quotationManager.getQuotations());
            maxPage = ((int) Math.ceil((double) quotations.size() / perPage)) - 1;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{page}", "" + page).replace("{max}", "" + maxPage);
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Next Page
            if (item.getFunction().equalsIgnoreCase("next-page") && page < maxPage) {
                page++;
                open();
                return;
            }

            // Previous Page
            if (item.getFunction().equalsIgnoreCase("previous-page") && page >= 0) {
                page--;
                open();
                return;
            }

            if (item instanceof QuotationItem) {
                // TODO


            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class QuotationItem extends PlaceholderItem<GeneratedQuotationList> {
        private final SimplePlaceholderItem noQuotation;

        public QuotationItem(ConfigurationSection config) {
            super(config);

            noQuotation = new SimplePlaceholderItem(config.getConfigurationSection("no-quotation"));
        }

        @Override
        public ItemStack getDisplayedItem(GeneratedQuotationList inv, int n) {
            int index = getSlots().size() * inv.page + n;

            // If above quotation number, display 'No quotation'
            if (index >= inv.quotations.size())
                return noQuotation.getDisplayedItem(inv, n);

            Quotation quotation = inv.quotations.get(index);

            // Displayed required quotation
            NBTItem nbt = NBTItem.get(super.getDisplayedItem(inv, n));
            nbt.addTag(new ItemTag("quotationId", quotation.getId()));
            return nbt.toItem();
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(GeneratedQuotationList inv, int n) {
            int index = getSlots().size() * inv.page + n;
            Quotation quotation = inv.quotations.get(index);

            Placeholders holders = new Placeholders();

            holders.register("company-name", quotation.getCompanyName());
            holders.register("stock-name", quotation.getStockName());
            // TODO remplacer les placeholders
            holders.register("current-price", 0);
            holders.register("week-low", quotation.getLowest(QuotationInfo.WEEK_TIME_OUT));
            holders.register("week-high", quotation.getHighest(QuotationInfo.WEEK_TIME_OUT));
            holders.register("month-low", quotation.getLowest(QuotationInfo.MONTH_TIME_OUT));
            holders.register("month-high", quotation.getHighest(QuotationInfo.MONTH_TIME_OUT));

            return holders;
        }
    }
}
