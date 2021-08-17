package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.Share;
import org.bukkit.event.HandlerList;

public class PlayerGenerateSharePaperEvent extends PlayerDataEvent {
    private final Share share;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a player closes a share from a certain quotation
     *
     * @param playerData Player closing the share
     * @param share      Share closed
     */
    public PlayerGenerateSharePaperEvent(PlayerData playerData, Share share) {
        super(playerData);

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
