package fr.lezoo.stonks.quotation;

import com.expression.parser.Parser;
import fr.lezoo.stonks.share.Share;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public class Dividends {
    private final Quotation quotation;
    private final String formula;
    private final long period;

    /**
     * Last time dividends were given to players
     */
    private long last;

    private static final Random random = new Random();

    /**
     * Public constructor for adding dividends to quotations
     *
     * @param formula The formula to calculate the amount of dividends
     *                to give to a player every X days
     * @param period  Dividends are given to players every X days
     */
    public Dividends(Quotation quotation, String formula, long period) {
        Validate.isTrue(period > 0, "Period must be positive");
        Validate.notNull(formula, "Dividend formula cannot be null");

        this.quotation = quotation;
        this.formula = formula;
        this.period = period;
    }

    /**
     * Loads information about stock dividends from a config
     */
    public Dividends(Quotation quotation, ConfigurationSection config) {
        this.quotation = quotation;
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

    public double applyFormula(Share share) {
        double random = Math.min(1, Math.max(-1, this.random.nextGaussian()));

        String parsed = new String(formula).replace("{amount}", String.valueOf(share.getAmount()))
                .replace("{random}", String.valueOf(random))
                .replace("{price}", String.valueOf(quotation.getPrice()));

        return Parser.simpleEval(parsed);
    }
}
