package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.Share;
import org.bukkit.event.HandlerList;

public class PlayerCloseShareEvent extends PlayerDataEvent {
    private final Quotation quotation;
    private final Share share;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a player closes a share from a certain quotation
     *
     * @param playerData Player closing the share
     * @param quotation  Quotation from which the share was sold
     * @param share      Share closed
     */
    public PlayerCloseShareEvent(PlayerData playerData, Quotation quotation, Share share) {
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
