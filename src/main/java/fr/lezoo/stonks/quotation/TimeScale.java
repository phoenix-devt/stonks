package fr.lezoo.stonks.quotation;

public enum TimeScale {
    HOUR(1000L * 60L * 60L),
    DAY(1000L * 24L * 60L * 60L),
    WEEK(1000L * 7L * 24L * 60L * 60L),
    MONTH(1000L * 30L * 24L * 60L * 60L),
    YEAR(1000L * 365L * 24L * 60L * 60L);

    private final long time;

    TimeScale(long time) {
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
}
