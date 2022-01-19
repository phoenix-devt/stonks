package fr.lezoo.stonks.share;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.UUID;

/**
 * One share can be bought by a player. This could be
 * saved in a hashMap if there weren't the leverage
 * parameter and the date at which it was bought
 */
public class Share {
    private final UUID uuid, owner;
    private final ShareType type;
    private final Quotation quotation;
    private final long timeStamp;
    private final double initialPrice;
    // if the share is closed it gets the final price and display it

    /**
     * The reason why the share was closed. Set to null
     * when it's still open
     */
    @Nullable
    private CloseReason closeReason;

    /**
     * Share price when it closed
     */
    private double sellPrice;

    /*
     * These fields can be modified by other plugins freely. maxPrice
     * and minPrice corresponds to the prices where it sells automatically
     */
    private double leverage, shares, maxPrice, minPrice;

    private double wallet;

    /**
     * Used when shares are being created, bought or shorted.
     *
     * @param type      Type of share
     * @param quotation Quotation
     * @param leverage  Multiplicative factor for the money made out of,
     *                  or lost by a share purchase
     * @param shares    Amount of shares purchased
     */
    public Share(ShareType type, UUID owner, Quotation quotation, double leverage, double shares, double maxPrice, double minPrice) {
        this(UUID.randomUUID(), owner, type, quotation, quotation.getPrice(), leverage, shares, maxPrice, minPrice, System.currentTimeMillis());
    }

    /**
     * Public construtor
     *
     * @param uuid         Share unique identifier
     * @param owner        Share owner (player) UUID
     * @param type         Type of share
     * @param quotation    Quotation the share is from
     * @param initialPrice Stock price when quotation was created
     * @param leverage     Multiplicative factor for the money made out of,
     *                     or lost by a share purchase
     * @param shares       Amount of shares purchased
     * @param timeStamp    Time of share creation (millis)
     */
    public Share(UUID uuid, UUID owner, ShareType type, Quotation quotation, double initialPrice, double leverage, double shares, double maxPrice, double minPrice, long timeStamp) {
        this.uuid = uuid;
        this.owner = owner;
        this.type = type;
        this.quotation = quotation;
        this.initialPrice = initialPrice;
        this.leverage = leverage;
        this.shares = shares;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.timeStamp = timeStamp;
    }

    /**
     * Loads a share from a config file
     */
    public Share(ConfigurationSection config) {
        this.uuid = UUID.fromString(config.getName());
        this.owner = UUID.fromString(config.getString("owner"));
        this.type = ShareType.valueOf(config.getString("type"));
        this.quotation = Stonks.plugin.quotationManager.get(config.getString("quotation"));
        this.shares = config.getDouble("shares");
        this.leverage = config.getDouble("leverage");
        this.timeStamp = config.getLong("timestamp");
        this.initialPrice = config.getDouble("initial");
        this.maxPrice = config.getDouble("max-price");
        this.maxPrice = config.getDouble("min-price");
        this.wallet = config.getDouble("wallet");
        this.closeReason = config.contains("close-reason") ? CloseReason.valueOf(config.getString("close-reason")) : null;
        this.sellPrice = isOpen() ? 0 : config.getDouble("sell-price");
    }

    /**
     * Reads a share from a NBT in which share data was saved.
     * See {@link fr.lezoo.stonks.item.SharePaper} to see the data format
     *
     * @param nbt Item NBT to read
     */
    public Share(UUID owner, PersistentDataContainer nbt) {

        // Info generated randomly
        this.owner = owner;
        this.uuid = UUID.randomUUID();

        // Mandatory info
        this.type = ShareType.valueOf(nbt.get(Utils.namespacedKey("ShareType"), PersistentDataType.STRING));
        this.quotation = Objects.requireNonNull(Stonks.plugin.quotationManager.get(nbt.get(Utils.namespacedKey("StockId"), PersistentDataType.STRING)), "Could not find quotation");
        this.timeStamp = nbt.get(Utils.namespacedKey("ShareTimeStamp"), PersistentDataType.LONG);
        this.initialPrice = nbt.get(Utils.namespacedKey("ShareInitialPrice"), PersistentDataType.DOUBLE);

        // Non final info
        this.shares = nbt.get(Utils.namespacedKey("ShareAmount"), PersistentDataType.DOUBLE);
        this.leverage = nbt.get(Utils.namespacedKey("ShareLeverage"), PersistentDataType.DOUBLE);
        this.wallet = nbt.get(Utils.namespacedKey("ShareWallet"), PersistentDataType.DOUBLE);
        this.closeReason = nbt.has(Utils.namespacedKey("CloseReason"), PersistentDataType.STRING) ? CloseReason.valueOf(nbt.get(Utils.namespacedKey("CloseReason"), PersistentDataType.STRING)) : null;
        this.sellPrice = isOpen() ? 0 : nbt.get(Utils.namespacedKey("SellPrice"), PersistentDataType.DOUBLE);
    }

    public void saveInConfig(ConfigurationSection config) {
        config.set(uuid + ".type", type.name());
        config.set(uuid + ".owner", owner.toString());
        config.set(uuid + ".quotation", quotation.getId());
        config.set(uuid + ".shares", shares);
        config.set(uuid + ".leverage", leverage);
        config.set(uuid + ".timestamp", timeStamp);
        config.set(uuid + ".initial", initialPrice);
        config.set(uuid + ".max-price", maxPrice);
        config.set(uuid + ".min-price", minPrice);
        config.set(uuid + ".wallet", wallet);
        if (!isOpen()) {
            config.set(uuid + ".close-reason", closeReason.name());
            config.set(uuid + ".sell-price", sellPrice);
        }
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public UUID getOwner() {
        return owner;
    }

    public ShareType getType() {
        return type;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public double getLeverage() {
        return leverage;
    }

    public double getAmount() {
        return shares;
    }

    /**
     * @return Time (in millis) at which the share was created
     */
    public long getCreationTime() {
        return timeStamp;
    }

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setAmount(double shares) {
        this.shares = shares;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }

    /**
     * @return Money given by dividends waiting to be claimed by the player
     */
    public double getWallet() {
        return wallet;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public double getMinPrice() {
        return minPrice;
    }

    @NotNull
    public CloseReason getCloseReason() {
        return Objects.requireNonNull(closeReason, "Share is open");
    }

    public double getShares() {
        return shares;
    }

    public boolean isOpen() {
        return closeReason == null;
    }

    /**
     * Closing a share differs from saving it. Closing a share
     * freezes its sell price. Claiming it gives the money
     * back to the player.
     * <p>
     * Closed shares with negative benefits should be automatically
     * claimed by the player who has no interest in manually
     * claiming them (resulting in a money loss)
     *
     * @param closeReason Why the share was closed
     */
    public void close(@NotNull CloseReason closeReason) {
        Validate.isTrue(isOpen(), "Share is already closed");
        this.closeReason = Objects.requireNonNull(closeReason, "Reason cannot be null");
        this.sellPrice = quotation.getPrice();
    }

    public void setWallet(double wallet) {
        this.wallet = wallet;
    }

    /**
     * Called when dividends are applied to the share.
     *
     * @param gain Money that can be claimed by the player.
     */
    public void addToWallet(double gain) {
        Validate.isTrue(gain >= 0, "Gain must be positive");

        wallet += gain;
    }

    /**
     * Does not take dividends wallet into account
     *
     * @param taxRate Rate of tax on benefits
     * @return Money earned by the player if he were to close
     * this share right now. This might return a negative
     * The share cannot be valued negatively
     */
    public double getCloseEarning(double taxRate) {
        return Math.max(calculateGain(taxRate) + initialPrice * shares,0);
    }

    /**
     * Does not take dividends wallet into account
     *
     * @return Money gained by the player (may be negative)
     * if they were to claim the share right now
     */
    public double calculateGain(double taxRate) {

        // Find applicable share price
        double sharePrice = isOpen() ? quotation.getPrice() : sellPrice;

        // <Difference in price between when it was bought and now> * <leverage> * <number of shares>
        double gain = (sharePrice - initialPrice) * (type == ShareType.SHORT ? -1 : 1) * leverage * shares;

        // Tax on benefits only
        if (gain > 0)
            gain *= 1 - taxRate;

        return gain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Share share = (Share) o;
        return Objects.equals(uuid, share.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
