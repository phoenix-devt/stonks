package fr.lezoo.stonks.util.message;

import org.apache.commons.lang.Validate;

public enum Language {
    SHARE_TYPE_SHORT("Short"),
    SHARE_TYPE_NORMAL("Normal"),
    ;

    private String message;

    private Language(String message) {
        this.message = message;
    }

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * @return Cached language piece
     */
    public String getCached() {
        return message;
    }

    public void update(String format) {
        Validate.notNull(this.message = format, "Could not read message format");
    }
}