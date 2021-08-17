package fr.lezoo.stonks.share;

import fr.lezoo.stonks.util.message.Language;

public enum ShareType {

    /**
     * Short selling means you gain money if
     * the stock price later decreases
     */
    SHORT,

    /**
     * Gain money if stock price increases
     */
    NORMAL;

    public String getTranslation() {
        return Language.valueOf("SHARE_TYPE_" + name()).getCached();
    }
}
