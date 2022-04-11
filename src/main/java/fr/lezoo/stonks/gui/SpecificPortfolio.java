package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.PlayerClaimShareEvent;
import fr.lezoo.stonks.api.event.PlayerGenerateSharePaperEvent;
import fr.lezoo.stonks.gui.objects.EditableInventory;
import fr.lezoo.stonks.gui.objects.GeneratedInventory;
import fr.lezoo.stonks.gui.objects.item.InventoryItem;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.gui.objects.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.ExchangeType;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

        if (function.equalsIgnoreCase("switch-viewed-shares"))
            return new SwitchViewedSharesItem(config);

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
        private boolean displayOpenShares = true;
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
            shares.addAll(playerData.getShares(quotation, displayOpenShares));
            maxPage = Math.max(((int) Math.ceil((double) shares.size() / perPage)) - 1, 0);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{name}", quotation.getName())
                    .replace("{share-status}", displayOpenShares ? "open" : "closed")
                    .replace("{page}", "" + (page + 1))
                    .replace("{max}", "" + (maxPage + 1));
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Switch viewed shares
            if (item instanceof SwitchViewedSharesItem) {
                this.displayOpenShares = !displayOpenShares;
                updateInventoryData();
                open();
                return;
            }

            // Back to list
            if (item instanceof BackItem) {
                Stonks.plugin.configManager.QUOTATION_LIST.generate(playerData).open();
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

                ItemStack itemStack = event.getCurrentItem();
                PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
                String shareId = container.get(new NamespacedKey(Stonks.plugin, "share_id"), PersistentDataType.STRING);
                if (shareId == null || shareId.isEmpty())
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

                    Message.GET_SHARE_PAPER.format("name", quotation.getName(),
                            "shares", Utils.fourDigits.format(share.getOrderInfo().getAmount())).send(player);

                    updateInventoryData();
                    open();

                } else if (event.getAction() == InventoryAction.PICKUP_ALL) {
                    PlayerClaimShareEvent called = new PlayerClaimShareEvent(playerData, share);
                    Bukkit.getPluginManager().callEvent(called);
                    if (called.isCancelled())
                        return;

                    // Close and claim share
                    double taxRate = playerData.getTaxRate();
                    double gain = share.calculateGain(taxRate), earned = share.getCloseEarning(taxRate);
                    Message.CLOSE_SHARES.format("shares", Utils.fourDigits.format(share.getOrderInfo().getAmount()),
                            "name", quotation.getName(),
                            "gain", Utils.formatGain(gain)).send(player);

                    // Virtual quotation
                    if (share.getQuotation().isVirtual()) {
                        Stonks.plugin.economy.depositPlayer(player, earned);
                        playerData.unregisterShare(share);

                        // Physical quotation
                    } else {
                        ExchangeType exchangeType = share.getQuotation().getExchangeType();
                        int realGain = (int) Math.floor(earned);
                        ItemStack giveItem = new ItemStack(exchangeType.getMaterial());
                        ItemMeta meta=giveItem.getItemMeta();
                        meta.setCustomModelData(exchangeType.getModelData());
                        giveItem.setItemMeta(meta);
                        while (realGain >= 0) {
                            int withdraw = Math.min(realGain, 64);
                            player.getInventory().addItem();
                            realGain -= withdraw;
                            playerData.unregisterShare(share);
                        }
                    }

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

    public class SwitchViewedSharesItem extends InventoryItem<GeneratedSpecificPortfolio> {
        private final InventoryItem showOpen;

        public SwitchViewedSharesItem(ConfigurationSection config) {
            super(config);

            showOpen = new SimpleItem(config.getConfigurationSection("show-open"));
        }

        @Override
        public ItemStack getDisplayedItem(GeneratedSpecificPortfolio inv, int n) {
            return inv.displayOpenShares ? super.getDisplayedItem(inv, n) : showOpen.getDisplayedItem(inv, n);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedSpecificPortfolio inv, int n) {
            return new Placeholders();
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

            ItemStack itemStack = super.getDisplayedItem(inv, n);
            ItemMeta meta = itemStack.getItemMeta();
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(Stonks.plugin, "share_id"), PersistentDataType.STRING, share.getUniqueId().toString());
            itemStack.setItemMeta(meta);

            return itemStack;
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

            holders.register("name", inv.quotation.getName());
            holders.register("leverage", Utils.fourDigits.format(share.getOrderInfo().getLeverage()));
            holders.register("amount", format.format(share.getOrderInfo().getAmount()));
            holders.register("min-price",share.getStringMinPrice());
            holders.register("max-price",share.getStringMaxPrice());
            holders.register("current-stock", format.format(inv.quotation.getPrice()));
            holders.register("initial-stock", format.format(share.getInitialPrice()));

            double taxRate = inv.getPlayerData().getTaxRate();
            holders.register("initial-share", format.format(share.getInitialPrice() * share.getOrderInfo().getAmount()));
            holders.register("current-share", format.format(share.getCloseEarning(taxRate)));
            holders.register("gain", Utils.formatGain(share.calculateGain(taxRate)));

            return holders;
        }
    }
}
