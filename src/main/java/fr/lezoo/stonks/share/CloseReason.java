package fr.lezoo.stonks.share;

public enum CloseReason {

    /**
     * When a share automatically closes due to user preference
     */
    AUTOMATIC,

    /**
     * When a share automatically closes as its value hits zero
     * <p>
     * Shares like this aren't worth anything anymore since they won't
     * update in the future so they're just flushed upon player login
     */
    DEFICIT,

    /**
     * When a share was closed using the close command
     */
    COMMAND,

    /**
     * When a player closes a share manually
     */
    MANUAL,

    /**
     * Any other reason not listed here
     */
    OTHER;
}
