package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.version.NBTItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationTimeDisplay;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimpleItem;
import fr.lezoo.stonks.version.ItemTag;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the list of all quotations, when clicking one
 * you open a {@link SpecificPortfolio}
 */
public class PortfolioList extends EditableInventory {
    public PortfolioList() {
        super("portfolio-list");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.equalsIgnoreCase("quotation"))
            return new QuotationItem(config);

        if (function.equalsIgnoreCase("next-page"))
            return new NextPageItem(config);

        if (function.equalsIgnoreCase("previous-page"))
            return new PreviousPageItem(config);

        return new SimpleItem(config);
    }

    public GeneratedInventory generate(PlayerData player) {
        return new GeneratedPortfolioList(player, this);
    }

    public class GeneratedPortfolioList extends GeneratedInventory {
        private final List<Quotation> quotations = new ArrayList<>();
        private final int maxPage;

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        public GeneratedPortfolioList(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            int perPage = editable.getByFunction("quotation").getSlots().size();

            quotations.addAll(Stonks.plugin.quotationManager.getQuotations());
            maxPage = Math.max(((int) Math.ceil((double) quotations.size() / perPage)) - 1, 0);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{page}", "" + (page + 1)).replace("{max}", "" + (maxPage + 1));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Next Page
            if (item instanceof PreviousPageItem && page < maxPage) {
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

            if (item instanceof QuotationItem) {
                NBTItem nbt = NBTItem.get(event.getCurrentItem());
                String quotationId = nbt.getString("quotationId");
                Quotation quotation = Stonks.plugin.quotationManager.get(quotationId);
                Validate.notNull(quotation, "Could not find quotation with ID '" + quotationId + "'");

                Stonks.plugin.configManager.SPECIFIC_PORTFOLIO.generate(playerData, quotation).open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class NextPageItem extends SimpleItem<GeneratedPortfolioList> {
        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedPortfolioList inv) {
            return inv.page < inv.maxPage;
        }
    }

    public class PreviousPageItem extends SimpleItem<GeneratedPortfolioList> {
        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedPortfolioList inv) {
            return inv.page > 0;
        }
    }

    public class QuotationItem extends InventoryItem<GeneratedPortfolioList> {
        private final SimpleItem noQuotation;

        public QuotationItem(ConfigurationSection config) {
            super(config);

            noQuotation = new SimpleItem(config.getConfigurationSection("no-quotation"));
        }

        @Override
        public ItemStack getDisplayedItem(GeneratedPortfolioList inv, int n) {
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
        public Placeholders getPlaceholders(GeneratedPortfolioList inv, int n) {
            int index = getSlots().size() * inv.page + n;
            Quotation quotation = inv.quotations.get(index);

            Placeholders holders = new Placeholders();

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("company", quotation.getName());
            holders.register("price", format.format(quotation.getPrice()));
            holders.register("week-low", format.format(quotation.getLowest(QuotationTimeDisplay.WEEK)));
            holders.register("week-high", format.format(quotation.getHighest(QuotationTimeDisplay.WEEK)));
            holders.register("month-low", format.format(quotation.getLowest(QuotationTimeDisplay.MONTH)));
            holders.register("month-high", format.format(quotation.getHighest(QuotationTimeDisplay.MONTH)));

            holders.register("owned", "" + inv.getPlayerData().countShares(quotation));

            return holders;
        }
    }
}
