package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.EditableInventory;
import fr.lezoo.stonks.gui.objects.GeneratedInventory;
import fr.lezoo.stonks.gui.objects.item.InventoryItem;
import fr.lezoo.stonks.gui.objects.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.ShareStatus;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;


public class ShareStatusMenu extends EditableInventory {
    public ShareStatusMenu() {
        super("share-status-menu");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {
        if (function.equalsIgnoreCase("open-shares"))
            return new OpenShareItem(config);
        if (function.equalsIgnoreCase("closed-shares"))
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
            Stonks.plugin.configManager.PORTFOLIO_LIST.generate(playerData, item instanceof OpenShareItem ? ShareStatus.OPEN : ShareStatus.CLOSED).open();
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




