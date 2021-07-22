package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.PlaceholderItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimplePlaceholderItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class QuotationPortfolio extends EditableInventory {
    public QuotationPortfolio() {
        super("portfolio");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {

        if (function.equalsIgnoreCase("quotation"))
            return new QuotationItem(config);

        return new SimplePlaceholderItem(config);
    }

    @Override
    public GeneratedInventory generate(PlayerData player) {
        return new GeneratedQuotationList(player, this);
    }

    public class GeneratedQuotationList extends GeneratedInventory {
        private final List<Quotation> quotations = new ArrayList<>();

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        public GeneratedQuotationList(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);

            quotations.addAll(Stonks.plugin.quotationManager.getQuotations());
        }

        @Override
        public String calculateName() {
            // TODO translation
            return "Quotations ("+(page+1)+")";
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Next Page
            if (item.getFunction().equalsIgnoreCase("next-page")) {
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

            // Displayed required quotation
            return super.getDisplayedItem(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedQuotationList inv, int n) {
            int index = getSlots().size() * inv.page + n;
            Quotation quotation = inv.quotations.get(index);

            Placeholders holders = new Placeholders();

            holders.register("name", "");

            return holders;
        }
    }
}
