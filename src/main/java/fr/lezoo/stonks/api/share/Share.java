package fr.lezoo.stonks.api.share;

import fr.lezoo.stonks.api.quotation.Quotation;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;
import java.util.UUID;

/**
 * One share can be bought by a player. This could be
 * saved in a hashMap if there weren't the leverage
 * parameter and the date at which it was bought
 */
public class Share {
    private final UUID uuid = UUID.randomUUID();
    private final ShareType type;
    private final long timeStamp;
    private final double initialPrice;

    private double leverage, shares;

    /**
     * @param type      Type of share
     * @param quotation Quotation the share is bought from
     * @param leverage  Multiplicative factor for the money made out of,
     *                  or lost by a share purchase
     * @param shares    Amount of shares purchased
     * @param timeStamp When the share was bought
     */
    public Share(ShareType type, Quotation quotation, double leverage, double shares, long timeStamp) {
        this.type = type;
        this.initialPrice = quotation.getPrice();
        this.leverage = leverage;
        this.shares = shares;
        this.timeStamp = timeStamp;
    }

    public Share(ConfigurationSection config) {
        this.type = ShareType.valueOf(config.getString("type"));
        this.leverage = config.getDouble("leverage");
        this.shares = config.getDouble("shares");
        this.timeStamp = config.getLong("timestamp");
        this.initialPrice = config.getLong("initial");
    }

    /**
     * Public constructor when creating a share. The time stamp
     * is the time at which this instance was created
     */
    public Share(ShareType type, Quotation quotation, double leverage, double shares) {
        this(type, quotation, leverage, shares, System.currentTimeMillis());
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public ShareType getType() {
        return type;
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
     * @param quotation The quotation the share was from bought
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
