package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.EditableInventory;
import fr.lezoo.stonks.gui.objects.GeneratedInventory;
import fr.lezoo.stonks.gui.objects.item.InventoryItem;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.gui.objects.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
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
 * Displays the list of all quotations, when clicking one
 * you get the menu where you can buy or sell shares
 * using the custom {@link QuotationShareMenu} GUI
 */
public class QuotationList extends EditableInventory {
    public QuotationList() {
        super("quotation-list");
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
        return new GeneratedQuotationList(player, this);
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
            maxPage = Math.max(((int) Math.ceil((double) quotations.size() / perPage)) - 1, 0);
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

            if (item instanceof QuotationItem) {
                ItemStack itemStack = event.getCurrentItem();
                PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
                String quotationId = container.get(new NamespacedKey(Stonks.plugin, "quotation_id"), PersistentDataType.STRING);
                if (quotationId == null || quotationId.isEmpty())
                    return;

                Quotation quotation = Stonks.plugin.quotationManager.get(quotationId);
                if (event.getAction() == InventoryAction.PICKUP_ALL)
                    Stonks.plugin.configManager.QUOTATION_SHARE.generate(playerData, quotation).open();
                if (event.getAction() == InventoryAction.PICKUP_HALF)
                    Stonks.plugin.configManager.SPECIFIC_PORTFOLIO.generate(playerData, quotation).open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class NextPageItem extends SimpleItem<GeneratedQuotationList> {
        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedQuotationList inv) {
            return inv.page < inv.maxPage;
        }
    }

    public class PreviousPageItem extends SimpleItem<GeneratedQuotationList> {
        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedQuotationList inv) {
            return inv.page > 0;
        }
    }

    public class QuotationItem extends InventoryItem<GeneratedQuotationList> {
        private final InventoryItem none, physicalQuotation;

        public QuotationItem(ConfigurationSection config) {
            super(config);

            none = new SimpleItem(config.getConfigurationSection("none"));
            physicalQuotation = new SubQuotationItem(config.getConfigurationSection("physical"), this);
        }

        @Override
        public ItemStack getDisplayedItem(GeneratedQuotationList inv, int n) {
            int index = getSlots().size() * inv.page + n;

            // If above quotation number, display 'No quotation'
            if (index >= inv.quotations.size())
                return none.getDisplayedItem(inv, n);

            Quotation quotation = inv.quotations.get(index);

            // Displayed required quotation
            ItemStack displayed = quotation.isVirtual() ? super.getDisplayedItem(inv, n) : physicalQuotation.getDisplayedItem(inv, n);
            ItemMeta meta = displayed.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Stonks.plugin, "quotation_id"), PersistentDataType.STRING, quotation.getId());
            displayed.setItemMeta(meta);

            return displayed;
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

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("company", quotation.getCompany());
            holders.register("price", format.format(quotation.getPrice()));
            holders.register("day-low", format.format(quotation.getLowest(TimeScale.DAY)));
            holders.register("day-high", format.format(quotation.getHighest(TimeScale.DAY)));
            holders.register("week-low", format.format(quotation.getLowest(TimeScale.WEEK)));
            holders.register("week-high", format.format(quotation.getHighest(TimeScale.WEEK)));
            holders.register("month-low", format.format(quotation.getLowest(TimeScale.MONTH)));
            holders.register("month-high", format.format(quotation.getHighest(TimeScale.MONTH)));
            holders.register("exchange-type", quotation.isVirtual() ? "money" : quotation.getExchangeType().toString().toLowerCase());
            holders.register("quotation-type", quotation.getClass().getSimpleName());
            return holders;
        }

        public class SubQuotationItem extends InventoryItem<GeneratedQuotationList> {
            private final InventoryItem parent;

            public SubQuotationItem(ConfigurationSection config, InventoryItem parent) {
                super(config);

                this.parent = parent;
            }

            @Override
            public Placeholders getPlaceholders(GeneratedQuotationList inv, int n) {
                return parent.getPlaceholders(inv, n);
            }
        }
    }
}
