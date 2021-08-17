package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.version.NBTItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.api.event.PlayerCloseShareEvent;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.util.message.Message;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimpleItem;
import fr.lezoo.stonks.version.ItemTag;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Displays all your shares from a SPECIFIC quotation
 */
public class SpecificPortfolio extends EditableInventory {
    public SpecificPortfolio() {
        super("specific-portfolio");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.equalsIgnoreCase("share"))
            return new ShareItem(config);

        if (function.equalsIgnoreCase("next-page"))
            return new NextPageItem(config);

        if (function.equalsIgnoreCase("previous-page"))
            return new PreviousPageItem(config);

        return new SimpleItem(config);
    }

    public GeneratedInventory generate(PlayerData player, Quotation quotation) {
        return new GeneratedSpecificPortfolio(player, quotation, this);
    }

    public class GeneratedSpecificPortfolio extends GeneratedInventory {
        private final Quotation quotation;
        private final List<Share> shares = new ArrayList<>();
        private final int maxPage;

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        public GeneratedSpecificPortfolio(PlayerData playerData, Quotation quotation, EditableInventory editable) {
            super(playerData, editable);

            // Get amount of shares displayed per page
            int perPage = editable.getByFunction("share").getSlots().size();

            this.quotation = quotation;
            shares.addAll(playerData.getShares(quotation));
            maxPage = Math.max(((int) Math.ceil((double) shares.size() / perPage)) - 1, 0);
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

            if (item instanceof ShareItem) {
                NBTItem nbt = NBTItem.get(event.getCurrentItem());
                String shareId = nbt.getString("shareId");
                Share share = playerData.getShareById(quotation, UUID.fromString(shareId));

                PlayerCloseShareEvent called = new PlayerCloseShareEvent(playerData, quotation, share);
                Bukkit.getPluginManager().callEvent(called);
                if (called.isCancelled())
                    return;

                // Close share
                double gain = share.calculateGain(quotation), earned = share.getCloseEarning(quotation);
                Message.CLOSE_SHARES.format("shares", "" + share.getAmount(),
                        "company", quotation.getCompanyName(),
                        "gain", Utils.formatGain(gain)).send(player);
                Stonks.plugin.economy.depositPlayer(player, earned);
                playerData.getShares(quotation).remove(share);

                Stonks.plugin.configManager.QUOTATION_SHARE.generate(playerData, quotation).open();
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class NextPageItem extends SimpleItem<GeneratedSpecificPortfolio> {
        public NextPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedSpecificPortfolio inv) {
            return inv.page < inv.maxPage;
        }
    }

    public class PreviousPageItem extends SimpleItem<GeneratedSpecificPortfolio> {
        public PreviousPageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public boolean isDisplayed(GeneratedSpecificPortfolio inv) {
            return inv.page > 0;
        }
    }

    public class ShareItem extends InventoryItem<GeneratedSpecificPortfolio> {
        private final SimpleItem noShare;

        public ShareItem(ConfigurationSection config) {
            super(config);

            noShare = new SimpleItem(config.getConfigurationSection("no-share"));
        }

        @Override
        public ItemStack getDisplayedItem(GeneratedSpecificPortfolio inv, int n) {
            int index = getSlots().size() * inv.page + n;

            // If above quotation number, display 'No share'
            if (index >= inv.shares.size())
                return noShare.getDisplayedItem(inv, n);

            Share share = inv.shares.get(index);

            // Displayed required quotation
            NBTItem nbt = NBTItem.get(super.getDisplayedItem(inv, n));
            nbt.addTag(new ItemTag("shareId", share.getUniqueId()));
            return nbt.toItem();
        }

        @Override
        public boolean hasDifferentDisplay() {
            return true;
        }

        @Override
        public Placeholders getPlaceholders(GeneratedSpecificPortfolio inv, int n) {
            int index = getSlots().size() * inv.page + n;
            Share share = inv.shares.get(index);

            Placeholders holders = new Placeholders();

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("company-name", inv.quotation.getCompanyName());
            holders.register("stock-name", inv.quotation.getStockName());

            holders.register("current-stock", inv.quotation.getPrice());
            holders.register("initial-stock", share.getInitialPrice());

            holders.register("initial-share", share.getInitialPrice() * share.getAmount());
            holders.register("gain", share.calculateGain(inv.quotation));

            return holders;
        }
    }
}
