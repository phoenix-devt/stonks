package fr.lezoo.stonks.stock;

import org.bukkit.configuration.ConfigurationSection;

/**
 * We both save the price of the stock and
 * the time stamp at which that price was evaluated
 */
public class StockInfo {
    private final long timeStamp;
    private final double price;

    public StockInfo(long time, double price) {
        this.timeStamp = time;
        this.price = price;
    }

    public StockInfo(ConfigurationSection section) {
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
