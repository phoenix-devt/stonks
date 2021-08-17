package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.PlayerCloseShareEvent;
import fr.lezoo.stonks.api.event.PlayerGenerateSharePaperEvent;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import fr.lezoo.stonks.version.ItemTag;
import fr.lezoo.stonks.version.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
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

        if (function.equalsIgnoreCase("back"))
            return new BackItem(config);

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
        private final int perPage;

        // Page indexing arbitrarily starts at 0
        private int page = 0;

        private final List<Share> shares = new ArrayList<>();
        private int maxPage;

        public GeneratedSpecificPortfolio(PlayerData playerData, Quotation quotation, EditableInventory editable) {
            super(playerData, editable);

            // Get amount of shares displayed per page
            this.perPage = editable.getByFunction("share").getSlots().size();
            this.quotation = quotation;

            updateInventoryData();
        }

        private void updateInventoryData() {
            shares.clear();
            shares.addAll(playerData.getShares(quotation));
            maxPage = Math.max(((int) Math.ceil((double) shares.size() / perPage)) - 1, 0);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{page}", "" + (page + 1)).replace("{max}", "" + (maxPage + 1));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Back to list
            if (item instanceof BackItem) {
                Stonks.plugin.configManager.PORTFOLIO_LIST.generate(playerData).open();
                return;
            }

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
                String shareId = nbt.getString("ShareId");
                if (shareId.isEmpty())
                    return;

                Share share = playerData.getShareById(quotation, UUID.fromString(shareId));

                if (event.getAction() == InventoryAction.PICKUP_HALF) {
                    PlayerGenerateSharePaperEvent called = new PlayerGenerateSharePaperEvent(playerData, share);
                    Bukkit.getPluginManager().callEvent(called);
                    if (called.isCancelled())
                        return;

                    // Unregister share before
                    playerData.unregisterShare(share);

                    // Get and give bill
                    ItemStack paper = Stonks.plugin.configManager.sharePaper.build(playerData.getPlayer(), share);
                    for (ItemStack dropped : player.getInventory().addItem(paper).values())
                        player.getWorld().dropItem(player.getLocation(), dropped);

                    Message.GET_SHARE_PAPER.format("company", quotation.getCompanyName(),
                            "shares", Utils.fourDigits.format(share.getAmount())).send(player);

                    updateInventoryData();
                    open();

                } else if (event.getAction() == InventoryAction.PICKUP_ALL) {
                    PlayerCloseShareEvent called = new PlayerCloseShareEvent(playerData, share);
                    Bukkit.getPluginManager().callEvent(called);
                    if (called.isCancelled())
                        return;

                    // Close share
                    double gain = share.calculateGain(), earned = share.getCloseEarning();
                    Message.CLOSE_SHARES.format("shares", Utils.fourDigits.format(share.getAmount()),
                            "company", quotation.getCompanyName(),
                            "gain", Utils.formatGain(gain)).send(player);
                    Stonks.plugin.economy.depositPlayer(player, earned);
                    playerData.unregisterShare(share);

                    updateInventoryData();
                    open();
                }
            }
        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    public class BackItem extends SimpleItem<GeneratedSpecificPortfolio> {
        public BackItem(ConfigurationSection config) {
            super(config);
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
            nbt.addTag(new ItemTag("ShareId", share.getUniqueId().toString()));
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
            holders.register("leverage", Utils.fourDigits.format(share.getLeverage()));
            holders.register("amount", format.format(share.getAmount()));

            holders.register("current-stock", format.format(inv.quotation.getPrice()));
            holders.register("initial-stock", format.format(share.getInitialPrice()));

            holders.register("initial-share", format.format(share.getInitialPrice() * share.getAmount()));
            holders.register("current-share", format.format(share.getCloseEarning()));
            holders.register("gain", Utils.formatGain(share.calculateGain()));

            return holders;
        }
    }
}
