package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.stock.Stock;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a stock receives a price update
 */
public class StockPriceUpdateEvent extends Event {
    private final Stock stock;

    private static final HandlerList handlers = new HandlerList();

    public StockPriceUpdateEvent(Stock stock) {
        this.stock = stock;
    }

    public Stock getStock() {
        return stock;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
