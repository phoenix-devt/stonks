package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.Share;
import fr.lezoo.stonks.api.quotation.Quotation;
import org.bukkit.event.HandlerList;

public class PlayerSellShareEvent extends PlayerDataEvent {
    private final Quotation quotation;
    private final Share share;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a player sells a share from a certain quotation
     *
     * @param playerData Player selling the share
     * @param quotation  Quotation from which the share was sold
     * @param share      Share sold
     */
    public PlayerSellShareEvent(PlayerData playerData, Quotation quotation, Share share) {
        super(playerData);

        this.quotation = quotation;
        this.share = share;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
