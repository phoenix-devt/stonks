package fr.lezoo.stonks.share;

public enum CloseReason {

    /**
     * When a share automatically closes due to user preference
     */
    AUTOMATIC,

    /**
     * When a share automatically closes due to server
     * preference when hitting too much debts
     * <p>
     * Shares with this close reason are automatically claimed
     * when the user logs in to make sure they are claimed
     * after closing automatically
     */
    DEFICIT,

    /**
     * When a share was closed using the close command
     */
    COMMAND,

    /**
     * Any other reason not listed here
     */
    OTHER;
}
