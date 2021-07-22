package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.SimplePlaceholderItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class QuotationList extends EditableInventory {
    public QuotationList() {
        super("quotation-list");
    }

    @Override
    public InventoryItem load(String function, ConfigurationSection config) {


        return new SimplePlaceholderItem(config);
    }

    @Override
    public GeneratedInventory generate(PlayerData player) {
        return new GeneratedQuotationList(player, this);
    }

    public class GeneratedQuotationList extends GeneratedInventory {

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        public GeneratedQuotationList(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String calculateName() {
            // TODO translation
            return "Quotations";
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
}
