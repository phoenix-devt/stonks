package fr.lezoo.stonks.player;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.PlayerBuyShareEvent;
import fr.lezoo.stonks.quotation.ExchangeType;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.util.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerData {
    private final UUID uuid;
    private Player player;
    private double taxRate;

    // Data not saved when logging off
    private double leverage = 1;

    /**
     * Mapped shares the player bought from a particular quotation
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
            for (String quotationKey : config.getConfigurationSection("shares").getKeys(false)) {
                Set<Share> shares = new HashSet<>();

                for (String shareKey : config.getStringList("shares." + quotationKey)) {
                    Share share = Stonks.plugin.shareManager.get(UUID.fromString(shareKey));
                    if (share != null)
                        shares.add(share);
                }

                if (!shares.isEmpty())
                    this.shares.put(quotationKey, shares);
            }

        taxRate = config.contains("tax-rate") ? config.getDouble("tax-rate") : -1;
    }

    public void saveInConfig(FileConfiguration config) {

        // Remove old shares
        config.set("shares", null);

        // Save newest shares
        for (String quotationId : shares.keySet()) {
            List<String> toList = new ArrayList<>();
            shares.get(quotationId).forEach(share -> toList.add(share.getUniqueId().toString()));
            if (!toList.isEmpty())
                config.set("shares." + quotationId, toList);
        }
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

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        Validate.isTrue(leverage > 0, "Leverage must be >0");
        this.leverage = leverage;
    }

    /**
     * @return Owned shares from a specific quotation
     */
    public Set<Share> getShares(Quotation quotation) {
        return shares.getOrDefault(quotation.getId(), new HashSet<>());
    }

    /**
     * @return Owned shares from a specific quotation with a specific ShareStatus
     */
    public Set<Share> getShares(Quotation quotation, boolean open) {
        // We filter to only have the ones of the specified share Status
        return shares.getOrDefault(quotation.getId(), new HashSet<>())
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
     * @return Owned shares from all quotations
     */
    public Set<Share> getAllShares() {
        Set<Share> total = new HashSet<>();

        for (Set<Share> shares : this.shares.values())
            total.addAll(shares);

        return total;
    }

    public void giveShare(Share share) {
        if (!shares.containsKey(share.getQuotation().getId()))
            shares.put(share.getQuotation().getId(), new HashSet<>());

        // Register share (throws IAE and cancel if any error)
        Stonks.plugin.shareManager.register(share);

        // Add to shares list
        this.shares.get(share.getQuotation().getId()).add(share);
    }

    public void unregisterShare(Share share) {
        Set<Share> shares = this.shares.get(share.getQuotation().getId());
        if (shares == null)
            return;

        // Unregister share from manager
        Stonks.plugin.shareManager.unregister(share);

        // Remove from list
        shares.remove(share);
    }

    public Share getShareById(Quotation quotation, UUID uuid) {

        for (Share share : getShares(quotation))
            if (share.getUniqueId().equals(uuid))
                return share;

        throw new IllegalArgumentException("Could not find share with given ID");
    }

    /**
     * @return Counts the shares the player owns in a certain quotation
     */
    public double countShares(Quotation quotation) {
        double total = 0;

        for (Share share : getShares(quotation))
            total += share.getAmount();

        return total;
    }

    /**
     * @return Counts the shares the player owns
     */
    public double countShares() {
        double total = 0;

        for (Set<Share> shares : this.shares.values())
            total += shares.size();

        return total;
    }

    /**
     * Called when a player tries to buy a share. This checks for the
     * player's balance and calls a bukkit cancelable event.
     *
     * @param quotation Quotation to buy share from
     * @param type      Type of share
     * @param amount    Amount of shares bought
     * @return If the share was successfully bought or not
     */
    public boolean buyShare(Quotation quotation, ShareType type, double amount, double maxPrice, double minPrice) {
        double price = quotation.getPrice() * amount;

        //If it exchanges money
        if (!quotation.isVirtual()) {
            // Check for balance
            double bal = Stonks.plugin.economy.getBalance(player);
            if (bal < price) {
                Message.NOT_ENOUGH_MONEY.format("shares", amount, "left", Stonks.plugin.configManager.stockPriceFormat.format(price - bal)).send(player);
                return false;
            }

            // Check for Bukkit event
            Share share = new Share(type, player.getUniqueId(), quotation, leverage, amount, minPrice, maxPrice);

            PlayerBuyShareEvent called = new PlayerBuyShareEvent(this, share);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;

            // Remove from balance and buy shares
            giveShare(share);
            Stonks.plugin.economy.withdrawPlayer(player, price);
        } else {
            //We make the price an int cause we can't withdraw half items (makes the player lose a bit of money
            price = Math.ceil(price);

            int bal = 0;
            //We check the amount of the item the player has in his inventory (hte material is defined by custom model data and material
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack != null && new ExchangeType(itemStack.getType(),itemStack.getItemMeta().getCustomModelData()).equals(quotation.getExchangeType()))
                    bal += itemStack.getAmount();
            }
            if (bal < price) {
                Message.NOT_ENOUGH_MONEY.format("shares", amount, "left", Stonks.plugin.configManager.stockPriceFormat.format(price - bal)).send(player);
                return false;
            }
            // Check for Bukkit event
            Share share = new Share(type, player.getUniqueId(), quotation, leverage, amount, minPrice, maxPrice);

            PlayerBuyShareEvent called = new PlayerBuyShareEvent(this, share);
            Bukkit.getPluginManager().callEvent(called);
            if (called.isCancelled())
                return false;

            //We give the player the share
            giveShare(share);
            //We withdraw the amount of shares he bought
            for (ItemStack itemStack : player.getInventory().getContents()) {
                if (itemStack != null && itemStack.getType().equals(quotation.getExchangeType())) {
                    double withdraw = Math.min(itemStack.getAmount(), price);
                    itemStack.setAmount(itemStack.getAmount() - (int) withdraw);
                    price -= withdraw;
                }

            }

        }


        // Send player message
        (type == ShareType.NORMAL ? Message.BUY_SHARES : Message.SELL_SHARES).format(
                "shares", Stonks.plugin.configManager.shareFormat.format(amount),
                "price", Stonks.plugin.configManager.stockPriceFormat.format(price),
                "company", quotation.getCompany()).send(player);

        // Successfully bought
        return true;
    }

    public static PlayerData get(Player player) {
        return Stonks.plugin.playerManager.get(player);
    }
}
