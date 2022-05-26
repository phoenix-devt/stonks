package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.PlayerClaimShareEvent;
import fr.lezoo.stonks.api.event.PlayerGenerateSharePaperEvent;
import fr.lezoo.stonks.api.event.ShareClosedEvent;
import fr.lezoo.stonks.gui.objects.EditableInventory;
import fr.lezoo.stonks.gui.objects.GeneratedInventory;
import fr.lezoo.stonks.gui.objects.item.InventoryItem;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.gui.objects.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.CloseReason;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.stock.ExchangeType;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Displays all your shares from a SPECIFIC stock
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

    public GeneratedInventory generate(PlayerData player, Stock stock) {
        return new GeneratedSpecificPortfolio(player, stock, this);
    }

    public class GeneratedSpecificPortfolio extends GeneratedInventory implements StockInventory {
        private final Stock stock;
        private final int perPage;

        // Page indexing arbitrarily starts at 0
        private int page = 0;
        private boolean displayOpenShares = true;
        private final List<Share> shares = new ArrayList<>();
        private int maxPage;

        public GeneratedSpecificPortfolio(PlayerData playerData, Stock stock, EditableInventory editable) {
            super(playerData, editable);

            // Get amount of shares displayed per page
            this.perPage = editable.getByFunction("share").getSlots().size();
            this.stock = stock;

            updateInventoryData();
        }

        @NotNull
        @Override
        public Stock getStock() {
            return stock;
        }

        private void updateInventoryData() {
            shares.clear();
            shares.addAll(playerData.getShares(stock, displayOpenShares));
            maxPage = Math.max(((int) Math.ceil((double) shares.size() / perPage)) - 1, 0);
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{name}", stock.getName())
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
                Stonks.plugin.configManager.STOCK_LIST.generate(playerData).open();
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

                Share share = playerData.getShareById(stock, UUID.fromString(shareId));

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

                    Message.GET_SHARE_PAPER.format("name", stock.getName(),
                            "shares", Utils.fourDigits.format(share.getOrderInfo().getAmount())).send(player);

                    updateInventoryData();
                    open();

                } else if (event.getAction() == InventoryAction.PICKUP_ALL) {
                    PlayerClaimShareEvent called = new PlayerClaimShareEvent(playerData, share);
                    Bukkit.getPluginManager().callEvent(called);
                    if (called.isCancelled())
                        return;

                    // Close and claim share
                    final double taxRate = playerData.getTaxRate(), taxDeduction = playerData.getTaxDeduction();
                    final double[] array = share.calculateGain(taxDeduction, taxRate);
                    final double gain = array[0], earned = share.getCloseEarning(taxDeduction, taxRate);
                    Message.CLOSE_SHARES.format("shares", Stonks.plugin.configManager.shareFormat.format(share.getOrderInfo().getAmount()),
                            "name", stock.getName(),
                            "gain", Utils.formatGain(gain)).send(player);

                    // Update tax deduction for later
                    playerData.deductTax(Math.max(0, -gain * taxRate) - array[1]);

                    // Close and call event
                    share.close(CloseReason.MANUAL);
                    if (displayOpenShares)
                        Bukkit.getPluginManager().callEvent(new ShareClosedEvent(share));

                    // Virtual stock
                    if (share.getStock().isVirtual())
                        Stonks.plugin.economy.depositPlayer(player, earned);

                        // Physical stock
                    else {
                        ExchangeType exchangeType = share.getStock().getExchangeType();
                        int realGain = (int) Math.floor(earned);
                        ItemStack giveItem = new ItemStack(exchangeType.getMaterial());
                        if (exchangeType.hasModelData()) {
                            ItemMeta meta = giveItem.getItemMeta();
                            meta.setCustomModelData(exchangeType.getModelData());
                            giveItem.setItemMeta(meta);
                        }
                        while (realGain >= 0) {
                            int withdraw = Math.min(realGain, 64);
                            realGain -= withdraw;

                            // Give item
                            ItemStack clone = giveItem.clone();
                            clone.setAmount(withdraw);
                            player.getInventory().addItem(clone);
                        }
                    }

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

            // If above stock number, display 'No share'
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

            holders.register("name", inv.stock.getName());
            holders.register("leverage", Utils.fourDigits.format(share.getOrderInfo().getLeverage()));
            holders.register("amount", format.format(share.getOrderInfo().getAmount()));
            holders.register("min-price", share.getMinPriceAsString());
            holders.register("max-price", share.getMaxPriceAsString());
            holders.register("current-stock", format.format(inv.stock.getPrice()));
            holders.register("initial-stock", format.format(share.getInitialPrice()));
            holders.register("share-type", share.getType().getTranslation());

            final double taxRate = inv.getPlayerData().getTaxRate(), taxDeduction = inv.getPlayerData().getTaxDeduction();
            holders.register("initial-share", format.format(share.getInitialPrice() * share.getOrderInfo().getAmount()));
            holders.register("current-share", format.format(share.getCloseEarning(taxDeduction, taxRate)));
            holders.register("gain", Utils.formatGain(share.calculateGain(taxDeduction, taxRate)[0]));

            return holders;
        }
    }
}
