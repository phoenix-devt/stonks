package fr.lezoo.stonks.stock.handler;

import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.TimeScale;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Random;

public class FictiveStockHandler implements StockHandler {
    private final Stock stock;

    private double priceMultiplier;
    private final double initialMarketShares, volatility;

    /**
     * Sum of initial amount of shares PLUS shares bought by investors
     */
    private double totalMarketShares;

    private static final Random RANDOM = new Random();
    private static final double DEFAULT_VOLATILITY = .01;

    public FictiveStockHandler(Stock stock, double price, double initialMarketShares) {
        this.stock = stock;
        this.initialMarketShares = initialMarketShares;

        // We setup the price multiplier
        this.priceMultiplier = price / initialMarketShares;
        this.totalMarketShares = initialMarketShares;
        this.volatility = DEFAULT_VOLATILITY;
    }

    public FictiveStockHandler(Stock stock, ConfigurationSection config) {
        this.stock = stock;

        initialMarketShares = config.getDouble("initial-supply");
        volatility = config.getDouble("volatility", DEFAULT_VOLATILITY);
        totalMarketShares = config.contains("total-supply") ? config.getDouble("total-supply") : initialMarketShares;
        priceMultiplier = config.contains("price-multiplier") ? config.getDouble("price-multiplier") : stock.getPrice() / initialMarketShares;
    }

    @Override
    public double getCurrentPrice() {
        return computePrice(totalMarketShares);
    }

    @Override
    public void whenBought(ShareType type, double shares) {
        totalMarketShares += (type == ShareType.NORMAL ? 1 : -1) * shares;
    }

    @Override
    public double getSellPrice(Share share) {
        return computePrice(totalMarketShares + share.getShares() * (share.getType() == ShareType.NORMAL ? -1 : 1));
    }

    @Override
    public void saveInFile(ConfigurationSection config) {
        config.set("initial-supply", initialMarketShares);
        config.set("total-supply", totalMarketShares);
        config.set("price-multiplier", priceMultiplier);
        config.set("volatility", volatility);
    }

    /**
     * @param totalShares Amount of shares in the market
     * @return The price the stock should have at any moment
     */
    public double computePrice(double totalShares) {
        return priceMultiplier * (totalShares < initialMarketShares / 10 ? expBehaviour(totalShares) : totalShares);
    }

    private double expBehaviour(double totalSup) {
        return initialMarketShares / 10 * (Math.exp(totalSup - (initialMarketShares / 10)));
    }

    /**
     * In this mathematical model where the price only depends on the
     * amount of shares currently in the market, refreshPrice only applies
     * some randomness to current price to simulate more market actors.
     * <p>
     * A huge advantage of this model is that it's entirely independent
     * of time, no need to integrate over time with respect to the change
     * in demand.
     * <p>
     * If volatility equals 1 you can expect to have a 10% change in 1 hour.
     */
    @Override
    public void refreshPrice() {
        priceMultiplier *= 1 + (RANDOM.nextDouble() - 0.5) * volatility * Math.sqrt(stock.getRefreshPeriod()) /
                (Math.sqrt(10 * TimeScale.HOUR.getTime()));
    }
}
