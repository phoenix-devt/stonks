package fr.lezoo.stonks.api.event;

import fr.lezoo.stonks.share.Share;
import org.apache.commons.lang.Validate;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ShareClosedEvent extends Event {
    private final Share share;

    private static final HandlerList handlers = new HandlerList();

    /**
     * Called when a share is being closed automatically, either
     * because of user preference to automatically when reaching
     * high debt rate
     * <p>
     * Not to be mistaken for the {@link PlayerClaimShareEvent}
     *
     * @param share Share that was closed
     */
    public ShareClosedEvent(Share share) {
        this.share = share;

        Validate.isTrue(!share.isOpen(), "Share is not closed");
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
