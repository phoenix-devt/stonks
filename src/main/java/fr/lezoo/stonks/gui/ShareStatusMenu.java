package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class ShareStatusMenu extends EditableInventory {

    public ShareStatusMenu() {
        super("share-status-menu");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equalsIgnoreCase("open-shares menu"))
            return new OpenShareItem(config);
        if (function.equalsIgnoreCase("closed-shares menu"))
            return new ClosedShareItem(config);
        return null;
    }

    public GeneratedInventory generate(PlayerData player) {
        return new ShareStatusGeneratedInventory(player, this);
    }

    public class ShareStatusGeneratedInventory extends GeneratedInventory {

        public ShareStatusGeneratedInventory(PlayerData playerData, EditableInventory editable) {
            super(playerData, editable);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str;
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {
            if(item instanceof OpenShareItem) {
                Stonks.plugin.configManager.OPEN_PORTFOLIO_LIST.generate(playerData).open();
            }
            else if (item instanceof ClosedShareItem) {
                Stonks.plugin.configManager.CLOSED_PORTFOLIO_LIST.generate(playerData).open();
            }

        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            //Nothing
        }
    }

    public class OpenShareItem extends SimpleItem<ShareStatusGeneratedInventory> {

        public OpenShareItem(ConfigurationSection config) {
            super(config);
        }
    }


    public class ClosedShareItem extends SimpleItem<ShareStatusGeneratedInventory> {

        public ClosedShareItem(ConfigurationSection config) {
            super(config);
        }
    }

}




