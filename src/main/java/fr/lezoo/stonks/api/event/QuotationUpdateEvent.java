package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.api.quotation.Quotation;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a quotation receives a price update
 */
public class QuotationUpdateEvent extends Event {
    private final Quotation quotation;

    private static final HandlerList handlers = new HandlerList();

    public QuotationUpdateEvent(Quotation quotation) {
        this.quotation = quotation;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
