package fr.lezoo.stonks.api.share;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.quotation.Quotation;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.UUID;

/**
 * One share can be bought by a player. This could be
 * saved in a hashMap if there weren't the leverage
 * parameter and the date at which it was bought
 */
public class Share {
    private final UUID uuid, ownerUuid;
    private final ShareType type;
    private final Quotation quotation;
    private final long timeStamp;
    private final double initialPrice;


    //maxPrice and minPrice corresponds to the prices where it sells of automatically
    // These can be modified by other plugins freely
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
    public Share(ShareType type, UUID ownerUuid, Quotation quotation, double leverage, double shares, double maxPrice, double minPrice) {
        this(UUID.randomUUID(), ownerUuid, type, quotation, quotation.getPrice(), leverage, shares, maxPrice, minPrice, System.currentTimeMillis());
    }

    /**
     * Public construtor
     *
     * @param uuid         Share unique identifier
     * @param type         Type of share
     * @param quotation    Quotation the share is from
     * @param initialPrice Stock price when quotation was created
     * @param leverage     Multiplicative factor for the money made out of,
     *                     or lost by a share purchase
     * @param shares       Amount of shares purchased
     * @param timeStamp    Time of share creation (millis)
     */
    public Share(UUID uuid, UUID ownerUuid, ShareType type, Quotation quotation, double initialPrice, double leverage, double shares, double maxPrice, double minPrice, long timeStamp) {
        this.uuid = uuid;
        this.ownerUuid = ownerUuid;
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
        this.ownerUuid = UUID.fromString(config.getString("owneruuid"));
        this.type = ShareType.valueOf(config.getString("type"));
        this.quotation = Stonks.plugin.quotationManager.get(config.getString("quotation"));
        this.shares = config.getDouble("shares");
        this.leverage = config.getDouble("leverage");
        this.timeStamp = config.getLong("timestamp");
        this.initialPrice = config.getDouble("initial");
        this.maxPrice=config.getDouble("maxprice");
        this.maxPrice=config.getDouble("minprice");
        this.wallet = config.getDouble("wallet");
    }

    public void saveInConfig(ConfigurationSection config) {
        config.set(uuid.toString() + ".type", type.name());
        config.set(uuid.toString() + ".owneruuid", ownerUuid.toString());
        config.set(uuid.toString() + ".quotation", quotation.getId());
        config.set(uuid.toString() + ".shares", shares);
        config.set(uuid.toString() + ".leverage", leverage);
        config.set(uuid.toString() + ".timestamp", timeStamp);
        config.set(uuid.toString() + ".initial", initialPrice);
        config.set(uuid.toString() + ".maxPrice", maxPrice);
        config.set(uuid.toString() + ".minPrice", minPrice);
        config.set(uuid.toString() + ".wallet", wallet);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
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

    public double getInitialPrice() {
        return initialPrice;
    }

    public void setShares(double shares) {
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
     * @param quotation The quotation the share was bought from
     * @return Money earned by the player if he were to close
     * this share right now. This might return a negative
     */
    public double getCloseEarning(Quotation quotation) {
        return calculateGain(quotation) + initialPrice * shares;
    }

    /**
     * @return Money gained by the player (may be negative)
     * if he were to close the share right now
     */
    public double calculateGain(Quotation quotation) {

        // Difference in price between when it was bought and now
        double diff = (quotation.getPrice() - initialPrice) * (type == ShareType.SHORT ? -1 : 1);

        // Multiply by leverage and shares
        return diff * leverage * shares;
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
