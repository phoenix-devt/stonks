package fr.lezoo.stonks.api;

import fr.lezoo.stonks.api.quotation.Quotation;

/**
 * One share can be bought by a player. This could be
 * saved in a hashMap if there weren't the leverage
 * parameter and the date at which it was bought
 */
public class Share {
    private final Quotation quotation;
    private double leverage, shares;
    private final long timeStamp;

    /**
     * @param quotation Quotation that share was purchased from
     * @param leverage  Multiplicative factor for the money made out of,
     *                  or lost by a share purchase
     * @param shares    Amount of shares purchased
     * @param timeStamp When the share was bought
     */
    public Share(Quotation quotation, double leverage, double shares, long timeStamp) {
        this.quotation = quotation;
        this.leverage = leverage;
        this.shares = shares;
        this.timeStamp = timeStamp;
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

    public void setShares(double shares) {
        this.shares = shares;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }
}
