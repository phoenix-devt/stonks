package fr.lezoo.stonks.api;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.PlayerBuyShareEvent;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.share.Share;
import fr.lezoo.stonks.api.share.ShareType;
import fr.lezoo.stonks.api.util.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private Player player;

    // Data not saved when logging off
    private double leverage = 1;

    /**
     * Mapped shares the player bought from a particular quotation
     */
    private final Map<String, Set<Share>> shares = new HashMap<>();

    /**
     * The last time a player claimed dividends from a specific quotation
     */
    private final Map<String, Long> lastDividendClaim = new HashMap<>();

    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();

        loadFromConfig(new ConfigFile("/userdata", uuid.toString()).getConfig());
    }

    public void loadFromConfig(FileConfiguration config) {

        // Load shares from config file
        for (String quotationKey : config.getConfigurationSection("shares").getKeys(false)) {
            Set<Share> shares = new HashSet<>();

            for (String shareKey : config.getStringList("shares." + quotationKey))
                shares.add(Stonks.plugin.shareManager.get(UUID.fromString(shareKey)));

            if (!shares.isEmpty())
                this.shares.put(quotationKey, shares);
        }

        // Load dividends claim
        for (String quotationKey : config.getConfigurationSection("dividends").getKeys(false))
            lastDividendClaim.put(quotationKey, config.getLong("dividends." + quotationKey));
    }

    public void saveInConfig(FileConfiguration config) {

        // Remove old shares
        config.set("shares", null);

        // Save newest
        for (String quotationId : shares.keySet()) {
            List<String> toList = new ArrayList<>();
            shares.get(quotationId).forEach(share -> toList.add(share.getUniqueId().toString()));
            if (!toList.isEmpty())
                config.set("shares." + quotationId, toList);
        }

        // Save dividends claim
        for (String quotationId : lastDividendClaim.keySet())
            config.set("dividends." + quotationId, lastDividendClaim.get(quotationId));
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

    public void addShare(Quotation quotation, Share share) {
        if (!shares.containsKey(quotation.getId()))
            shares.put(quotation.getId(), new HashSet<>());

        this.shares.get(quotation.getId()).add(share);
    }

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        Validate.isTrue(leverage > 0, "Leverage must be >0");
        this.leverage = leverage;
    }

    public Set<Share> getShares(Quotation quotation) {
        return shares.getOrDefault(quotation.getId(), new HashSet<>());
    }

    /**
     * @return IDs of quotations the player has some shares of which
     */
    public Set<String> getShareKeys() {
        return shares.keySet();
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

        for (String quotationId : shares.keySet())
            for (Share share : shares.get(quotationId))
                total += share.getAmount();

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
    public boolean buyShare(Quotation quotation, ShareType type, double amount) {
        double price = quotation.getPrice() * amount;

        // Check for balance
        double bal = Stonks.plugin.economy.getBalance(player);
        if (bal < price) {
            Message.NOT_ENOUGH_MONEY.format("shares", amount, "left", Stonks.plugin.configManager.stockPriceFormat.format(price - bal)).send(player);
            return false;
        }

        // Check for Bukkit event
        Share share = new Share(type, quotation, leverage, amount);
        PlayerBuyShareEvent called = new PlayerBuyShareEvent(this, quotation, share);
        Bukkit.getPluginManager().callEvent(called);
        if (called.isCancelled())
            return false;

        // Remove from balance and buy shares
        addShare(quotation, share);
        Stonks.plugin.economy.withdrawPlayer(player, price);

        // Send player message
        (type == ShareType.POSITIVE ? Message.BUY_SHARES : Message.SELL_SHARES).format(
                "shares", Stonks.plugin.configManager.shareFormat.format(amount),
                "price", Stonks.plugin.configManager.stockPriceFormat.format(price),
                "company", quotation.getCompanyName()).send(player);

        // Successfully bought
        return true;
    }

    public static PlayerData get(Player player) {
        return Stonks.plugin.playerManager.get(player);
    }
}
