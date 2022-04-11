package fr.lezoo.stonks.quotation;

import com.expression.parser.Parser;
import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.share.Share;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public class Dividends {
    private final Quotation quotation;
    private final String formula;
    //period in days for the dividends
    private final int period;

    /**
     * Last time dividends were given to players
     */
    private long last;

    private static final Random random = new Random();


    public Dividends(Quotation quotation) {
        this(quotation,Stonks.plugin.configManager.dividendFormula,Stonks.plugin.configManager.dividendPeriod);
    }

    /**
     * Public constructor for adding dividends to quotations
     *
     * @param formula The formula to calculate the amount of dividends
     *                to give to a player every X days
     * @param period  Dividends are given to players every X days
     */
    public Dividends(Quotation quotation, String formula, int period) {
        Validate.isTrue(period > 0, "Period must be positive");
        Validate.notNull(formula, "Dividend formula cannot be null");

        this.quotation = quotation;
        this.formula = formula;
        this.period = period;
        this.last=System.currentTimeMillis();
    }

    /**
     * Loads information about stock dividends from a config
     */
    public Dividends(Quotation quotation, ConfigurationSection config) {
        this.quotation = quotation;
        this.formula = config.getString("formula");
        this.period = config.getInt("period");
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
        return System.currentTimeMillis() > last + period*1000*3600*24;
    }

    public double applyFormula(Share share) {
        double random = 2*this.random.nextDouble()-1;

        String parsed = formula.replace("{amount}", String.valueOf(share.getOrderInfo().getAmount()))
                .replace("{random}", String.valueOf(random))
                .replace("{price}", String.valueOf(quotation.getPrice()));

        return Parser.simpleEval(parsed);
    }
}
