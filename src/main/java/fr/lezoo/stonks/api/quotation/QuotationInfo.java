package fr.lezoo.stonks.api.quotation;

import org.bukkit.configuration.ConfigurationSection;

/**
 * We both save the price of the quotation and
 * the time stamp at which that price was evaluated
 */
public class QuotationInfo {
    private final long timeStamp;
    private final double price;

    public QuotationInfo(long time, double price) {
        this.timeStamp = time;
        this.price = price;
    }


    public QuotationInfo(ConfigurationSection section) {
        this.timeStamp = section.getLong("timestamp");
        this.price = section.getDouble("price");
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public double getPrice() {
        return price;
    }
}
