package fr.lezoo.stonks.api.quotation;

public enum QuotationTimeDisplay {
    QUARTERHOUR(1000L * 15L * 60L),
    HOUR(1000L * 60L * 60L),
    DAY(1000L * 24L * 60L * 60L),
    WEEK(1000L * 7L * 24L * 60L * 60L),
    MONTH(1000L * 30L * 24L * 60L * 60L),
    YEAR(1000L * 365L * 24L * 60L * 60L);

    private final long time;

    QuotationTimeDisplay(long time) {
        this.time = time;
    }

    /**
     * Integers might overflow so using longs
     *
     * @return The time in milliseconds corresponding
     */
    public long getTime() {
        return time;
    }

    /**
     * @return If given string matches any enum field name
     */
    public static boolean checkQuotationTimeDisplay(String s) {
        for (QuotationTimeDisplay quot : QuotationTimeDisplay.values())
            if (quot.toString().equals(s))
                return true;
        return false;
    }
}
