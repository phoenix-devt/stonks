package fr.lezoo.stonks.api;

import fr.lezoo.stonks.api.quotation.Quotation;

/**
 * One stock can be bought by a player. This could be
 * saved in a hashMap if there weren't the leverage
 * parameter.
 */
public class Stock {
    private final Quotation quotation;
    private double leverage, stocks;

    /**
     * @param quotation Quotation that stock was purchased from
     * @param leverage  Multiplicative factor for the money made out of,
     *                  or lost by a stock purchase
     * @param stocks    Amount of stocks purchased
     */
    public Stock(Quotation quotation, double leverage, double stocks) {
        this.quotation = quotation;
        this.leverage = leverage;
        this.stocks = stocks;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public double getLeverage() {
        return leverage;
    }

    public double getAmount() {
        return stocks;
    }

    public void setStocks(double stocks) {
        this.stocks = stocks;
    }

    public void setLeverage(double leverage) {
        this.leverage = leverage;
    }
}
