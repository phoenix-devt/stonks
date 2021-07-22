package fr.lezoo.stonks.api.share;

/**
 * One share can be bought by a player. This could be
 * saved in a hashMap if there weren't the leverage
 * parameter and the date at which it was bought
 */
public class Share {
    private final ShareType type;
    private double leverage, shares;
    private final long timeStamp;

    /**
     * @param type      Type of share
     * @param leverage  Multiplicative factor for the money made out of,
     *                  or lost by a share purchase
     * @param shares    Amount of shares purchased
     * @param timeStamp When the share was bought
     */
    public Share(ShareType type, double leverage, double shares, long timeStamp) {
        this.type = type;
        this.leverage = leverage;
        this.shares = shares;
        this.timeStamp = timeStamp;
    }

    /**
     * Public constructor when creating a share. The time stamp
     * is the time at which this instance was created
     */
    public Share(ShareType type, double leverage, double shares) {
        this(type, leverage, shares, System.currentTimeMillis());
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

    public void setShares(double shares) {
        this.shares = shares;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }
}
