package fr.lezoo.stonks.stock;

import com.expression.parser.Parser;
import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.share.Share;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public class Dividends {
    private final Stock stock;
    private final String formula;

    /**
     * Period of dividends, in days
     */
    private final int period;

    /**
     * Last time dividends were given to players
     */
    private long lastApplication;

    private static final Random RANDOM = new Random();

    /**
     * Default dividends
     */
    public Dividends(Stock stock) {
        this(stock, Stonks.plugin.configManager.defaultDividendFormula, Stonks.plugin.configManager.defaultDividendPeriod);
    }

    /**
     * Public constructor for adding dividends to stocks
     *
     * @param formula The formula to calculate the amount of dividends
     *                to give to a player every X days
     * @param period  Dividends are given to players every X days
     */
    public Dividends(Stock stock, String formula, int period) {
        Validate.isTrue(period > 0, "Period must be positive");
        Validate.notNull(formula, "Dividend formula cannot be null");

        this.stock = stock;
        this.formula = formula;
        this.period = period;
        this.lastApplication = System.currentTimeMillis();
    }

    /**
     * Loads information about stock dividends from a config
     */
    public Dividends(Stock stock, ConfigurationSection config) {
        this.stock = stock;
        this.formula = config.getString("formula");
        this.period = config.getInt("period");
        this.lastApplication = config.getLong("last");
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
        return lastApplication;
    }

    /**
     * The last time dividends were given to players.
     *
     * @param last Time stamp in millis
     */
    public void setLastApplication(long last) {
        this.lastApplication = last;
    }

    public boolean canGiveDividends() {
        return System.currentTimeMillis() > lastApplication + period * 1000 * 3600 * 24;
    }

    public double applyFormula(Share share) {
        double random = 2 * this.RANDOM.nextDouble() - 1;

        String parsed = formula.replace("{amount}", String.valueOf(share.getOrderInfo().getAmount()))
                .replace("{random}", String.valueOf(random))
                .replace("{price}", String.valueOf(stock.getPrice()));

        return Parser.simpleEval(parsed);
    }
}
