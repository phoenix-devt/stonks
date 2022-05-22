package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.Share;
import org.bukkit.event.HandlerList;

public class PlayerClaimShareEvent extends PlayerDataEvent {
    private final Share share;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a player claims a share from a certain stock.
     * It is called for both closed and open shares.
     * <p>
     * Not to be mistaken for the {@link ShareClosedEvent}.
     *
     * @param playerData Player closing the share
     * @param share      Share closed
     */
    public PlayerClaimShareEvent(PlayerData playerData, Share share) {
        super(playerData);

        this.share = share;
    }

    public Share getShare() {
        return share;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
