package fr.lezoo.stonks.player;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.PlayerBuyShareEvent;
import fr.lezoo.stonks.share.OrderInfo;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.stock.ExchangeType;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.util.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    private final UUID uuid;
    private Player player;
    private double taxRate;

    /**
     * Links stock IDs to order infos
     */
    private final Map<String, OrderInfo> orderInfos = new HashMap<>();

    private boolean isOnChatInput;

    public void setOnChatInput(boolean onChatInput) {
        isOnChatInput = onChatInput;
    }

    public boolean isOnChatInput() {
        return isOnChatInput;
    }

    /**
     * Mapped shares the player bought from a particular stock
     */
    private final Map<String, Set<Share>> shares = new HashMap<>();

    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        loadFromConfig(new ConfigFile("/userdata", uuid.toString()).getConfig());
    }

    public void loadFromConfig(FileConfiguration config) {
        // playerStatus=PlayerStatus.valueOf(config.getString("player-status").toUpperCase());
        // Load shares from config file
        if (config.contains("shares"))
            for (String stockKey : config.getConfigurationSection("shares").getKeys(false)) {
                Set<Share> shares = new HashSet<>();

                for (String shareKey : config.getStringList("shares." + stockKey)) {
                    Share share = Stonks.plugin.shareManager.get(UUID.fromString(shareKey));
                    if (share != null)
                        shares.add(share);
                }

                if (!shares.isEmpty())
                    this.shares.put(stockKey, shares);
            }

        taxRate = config.contains("tax-rate") ? config.getDouble("tax-rate") : -1;
    }

    public void saveInConfig(FileConfiguration config) {

        // Remove old shares
        config.set("shares", null);

        // Save newest shares
        for (String stockId : shares.keySet()) {
            List<String> toList = new ArrayList<>();
            shares.get(stockId).forEach(share -> toList.add(share.getUniqueId().toString()));
            if (!toList.isEmpty())
                config.set("shares." + stockId, toList);
        }
    }

    public boolean hasOrderInfo(String stockId) {
        return orderInfos.containsKey(stockId);
    }

    public void addOrderInfo(String stockId) {
        if (!hasOrderInfo(stockId))
            orderInfos.put(stockId, new OrderInfo());
    }

    public OrderInfo getOrderInfo(String stockId) {
        if (!this.hasOrderInfo(stockId))
            this.addOrderInfo(stockId);
        return orderInfos.get(stockId);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public void updatePlayer(Player player) {
        this.player = player;
    }

    /**
     * @return Owned shares from a specific stock
     */
    public Set<Share> getShares(Stock stock) {
        return shares.getOrDefault(stock.getId(), new HashSet<>());
    }

    /**
     * @return Owned shares from a specific stock with a specific ShareStatus
     */
    public Set<Share> getShares(Stock stock, boolean open) {
        // We filter to only have the ones of the specified share Status
        return shares.getOrDefault(stock.getId(), new HashSet<>())
                .stream()
                .filter((share) -> share.isOpen() == open)
                .collect(Collectors.toSet());
    }

    public double getTaxRate() {
        return taxRate == -1 ? Stonks.plugin.configManager.defaultTaxRate : taxRate;
    }

    /**
     * Tax rate must be between 0 and 1
     * <p>
     * When set to -1, it uses the default tax rate input in the config file
     *
     * @param taxRate Player tax rate on benefits from 0 to 1, or -1 if default
     */
    public void setTaxRate(double taxRate) {
        Validate.isTrue((taxRate >= 0 && taxRate <= 1) || taxRate == -1, "Tax rate must be positive");
        this.taxRate = taxRate;
    }

    /**
     * @return Owned shares from all stocks
     */
    public Set<Share> getAllShares() {
        Set<Share> total = new HashSet<>();

        for (Set<Share> shares : this.shares.values())
            total.addAll(shares);

        return total;
    }

    public void giveShare(Share share) {
        if (!shares.containsKey(share.getStock().getId()))
            shares.put(share.getStock().getId(), new HashSet<>());

        // Register share (throws IAE and cancel if any error)
        Stonks.plugin.shareManager.register(share);

        // Add to shares list
        this.shares.get(share.getStock().getId()).add(share);
    }

    public void unregisterShare(Share share) {
        Set<Share> shares = this.shares.get(share.getStock().getId());
        if (shares == null)
            return;

        // Unregister share from manager
        Stonks.plugin.shareManager.unregister(share);

        // Remove from list
        shares.remove(share);
    }

    public Share getShareById(Stock stock, UUID uuid) {

        for (Share share : getShares(stock))
            if (share.getUniqueId().equals(uuid))
                return share;

        throw new IllegalArgumentException("Could not find share with given ID");
    }

    /**
     * @return Counts the shares the player owns in a certain stock
     */
    public double countShares(Stock stock) {
        double total = 0;

        for (Share share : getShares(stock))
            total += share.getOrderInfo().getAmount();

        return total;
    }

    /**
     * @return Counts the shares the player o"wns
     */
    public double countShares() {
        double total = 0;

        for (Set<Share> shares : this.shares.values())
            total += shares.size();

        return total;
    }

    /**
     * Buys a share by using the orderinfo of the player if there is one
     */
    public boolean buyShare(Stock stock, ShareType type) {
        if (!hasOrderInfo(stock.getId())) {
            Message.NO_ORDER.format("stock-name", stock.getName()).send(player);
            return false;
        }
        OrderInfo orderInfo = getOrderInfo(stock.getId());
        double amount = orderInfo.getAmount();
        if (amount == 0) {
            Message.NO_AMOUNT.format("stock-name", stock.getName()).send(player);
            return false;
        }
        return buyShare(stock, type, amount, orderInfo.getLeverage(), orderInfo.hasMaxPrice() ? orderInfo.getMaxPrice() : Double.POSITIVE_INFINITY, orderInfo.hasMinPrice() ? orderInfo.getMinPrice() : 0);
    }

    /**
     * Used to buy a share using order info except the amount (for the fixed amount shares on the stockShareMenu GUI)
     */
    public boolean buyShare(Stock stock, ShareType type, double amount) {
        if (!hasOrderInfo(stock.getId()))
            return buyShare(stock, type, amount, 1, Double.POSITIVE_INFINITY, 0);

        OrderInfo orderInfo = getOrderInfo(stock.getId());
        return buyShare(stock, type, amount, orderInfo.getLeverage(), orderInfo.hasMaxPrice() ? orderInfo.getMaxPrice() : Double.POSITIVE_INFINITY, orderInfo.hasMinPrice() ? orderInfo.getMinPrice() : 0);
    }

    /**
     * Called when a player tries to buy a share. This checks for the
     * player's balance and calls a bukkit cancelable event.
     *
     * @param stock  Stock to buy share from
     * @param type   Type of share
     * @param amount Amount of shares bought
     * @return If the share was successfully bought or not
     */
    public boolean buyShare(Stock stock, ShareType type, double amount, int leverage, double maxPrice, double minPrice) {
        final double moneyPaid = amount * stock.getPrice();

        // If it exchanges money
        if (stock.isVirtual()) {

            // Check for balance
            double balance = Stonks.plugin.economy.getBalance(player);
            if (balance < moneyPaid) {
                Message.NOT_ENOUGH_MONEY.format("shares", amount, "left", Stonks.plugin.configManager.stockPriceFormat.format(moneyPaid - balance)).send(player);
                return false;
            }

            // Check for Bukkit event
            Share share = new Share(type, player.getUniqueId(), stock, leverage, amount, maxPrice, minPrice);

            PlayerBuyShareEvent called = new PlayerBuyShareEvent(this, share);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;

            // Remove from balance and buy shares
            giveShare(share);
            Stonks.plugin.economy.withdrawPlayer(player, moneyPaid);

        } else {

            // We make the price an int cause we can't withdraw half items (makes the player lose a bit of money
            long moneyPaidCeil = (long) Math.ceil(moneyPaid);

            // We check the amount of the item the player has in his inventory (the material is defined by custom model data and material
            int balance = 0;
            for (ItemStack itemStack : player.getInventory().getContents())
                if (itemStack != null && new ExchangeType(itemStack.getType(), itemStack.getItemMeta().hasCustomModelData() ? itemStack.getItemMeta().getCustomModelData() : 0).equals(stock.getExchangeType()))
                    balance += itemStack.getAmount();

            if (balance < moneyPaidCeil) {
                Message.NOT_ENOUGH_MONEY.format("shares", amount, "left", String.valueOf(moneyPaidCeil - balance)).send(player);
                return false;
            }

            // Check for Bukkit event
            Share share = new Share(type, player.getUniqueId(), stock, leverage, amount, minPrice, maxPrice);

            PlayerBuyShareEvent called = new PlayerBuyShareEvent(this, share);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;

            // We give the player the share
            giveShare(share);

            // We withdraw the amount of shares he bought
            for (ItemStack itemStack : player.getInventory().getContents())
                if (itemStack != null && new ExchangeType(itemStack.getType(), itemStack.getItemMeta().hasCustomModelData() ? itemStack.getItemMeta().getCustomModelData() : 0).equals(stock.getExchangeType())) {
                    double withdraw = Math.min(itemStack.getAmount(), moneyPaidCeil);
                    itemStack.setAmount(itemStack.getAmount() - (int) withdraw);
                    moneyPaidCeil -= withdraw;
                }
        }

        stock.getHandler().whenBought(type == ShareType.NORMAL ? amount : -amount);

        // Send player message
        (type == ShareType.NORMAL ? Message.BUY_SHARES : Message.SELL_SHARES).format(
                "shares", Stonks.plugin.configManager.shareFormat.format(amount),
                "price", Stonks.plugin.configManager.stockPriceFormat.format(moneyPaid),
                "name", stock.getName()).send(player);

        // Successfully bought
        return true;
    }

    public static PlayerData get(Player player) {
        return Stonks.plugin.playerManager.get(player);
    }
}
