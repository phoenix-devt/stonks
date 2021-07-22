package fr.lezoo.stonks.api.quotation;

/**
 * We both save the price of the quotation and
 * the time stamp at which that price was evaluated
 */
public class QuotationInfo {
    private final long timeStamp;
    private final double price;

    public static final long WEEK_TIME_OUT = 1000 * 60 * 60 * 24 * 7;
    public static final long MONTH_TIME_OUT = 1000 * 60 * 60 * 24 * 30;

    public QuotationInfo(long time, double price) {
        this.timeStamp = time;
        this.price = price;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public double getPrice() {
        return price;
    }
}
