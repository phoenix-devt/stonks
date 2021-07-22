package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.Stock;
import fr.lezoo.stonks.api.quotation.Quotation;
import org.bukkit.event.HandlerList;

public class PlayerSellStockEvent extends PlayerDataEvent {
    private final Quotation quotation;
    private final Stock stock;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a player sells a stock from a certain quotation
     *
     * @param playerData Player selling the stock
     * @param quotation  Quotation from which the stock was sold
     * @param stock      Stock sold
     */
    public PlayerSellStockEvent(PlayerData playerData, Quotation quotation, Stock stock) {
        super(playerData);

        this.quotation = quotation;
        this.stock = stock;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
