package fr.lezoo.stonks.api.quotation;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

public class Dividends {
    private final String formula;
    private final long period;

    /**
     * Last time dividends were given to players
     */
    private long last;

    /**
     * Public constructor for adding dividends to quotations
     *
     * @param formula The formula to calculate the amount of dividends
     *                to give to a player every X days
     * @param period  Dividends are given to players every X days
     */
    public Dividends(String formula, long period) {
        Validate.isTrue(period > 0, "Period must be positive");
        Validate.notNull(formula, "Dividend formula cannot be null");

        this.formula = formula;
        this.period = period;
    }

    /**
     * Loads information about stock dividens from a config
     */
    public Dividends(ConfigurationSection config) {
        this.formula = config.getString("formula");
        this.period = config.getLong("period");
        this.last = config.getLong("last");
    }

    public long getPeriod() {
        return period;
    }

    public String getFormula() {
        return formula;
    }

    /**
     * @return The last time dividends were given to players.
     */
    public long getLastApplication() {
        return last;
    }

    /**
     * The last time dividends were given to players.
     *
     * @param last Time stamp in millis
     */
    public void setLastApplication(long last) {
        this.last = last;
    }

    public boolean canGiveDividends() {
        return System.currentTimeMillis() > last + period;
    }
}
