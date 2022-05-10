package fr.lezoo.stonks.stock.handler;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.TimeScale;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FictiveStockHandler implements StockHandler {
    private final Stock stock;

    private double priceMultiplier;
    private final double initialMarketShares;

    /**
     * Sum of initial amount of shares PLUS shares bought by investors
     */
    private double totalMarketShares;

    private static final Random RANDOM = new Random();

    public FictiveStockHandler(Stock stock, double initialMarketShares) {
        this.stock = stock;
        this.initialMarketShares = initialMarketShares;

        // We setup the price multiplier
        this.priceMultiplier = stock.getPrice() / initialMarketShares;
        this.totalMarketShares = initialMarketShares;
    }

    public FictiveStockHandler(Stock stock, ConfigurationSection config) {
        this.stock = stock;

        initialMarketShares = config.getDouble("initial-supply");
        totalMarketShares = config.contains("total-supply") ? config.getDouble("total-supply") : initialMarketShares;
        priceMultiplier = config.contains("price-multiplier") ? config.getDouble("price-multiplier") : stock.getPrice() / initialMarketShares;
    }

    @Override
    public void refresh() {
        double price = computePrice(totalMarketShares);

        // We setup the price in the stock
        for (TimeScale time : TimeScale.values()) {

            // We get the list corresponding to the time
            List<StockInfo> workingData = new ArrayList<>();
            workingData.addAll(stock.getData(time));

            // This fixes an issue with empty working data
            long lastTimeStamp = workingData.isEmpty() ? 0 : workingData.get(workingData.size() - 1).getTimeStamp();

            // If the the latest data of workingData is too old we add another one
            if (System.currentTimeMillis() - lastTimeStamp > time.getTime() / Stock.BOARD_DATA_NUMBER) {
                workingData.add(new StockInfo(System.currentTimeMillis(), price));

                // If the list contains too much data we remove the older ones
                if (workingData.size() > Stock.BOARD_DATA_NUMBER)
                    workingData.remove(0);

                // We save the changes we made in the attribute
                stock.setData(time, workingData);
            }
        }
    }

    @Override
    public double getCurrentPrice() {
        return computePrice(totalMarketShares);
    }

    @Override
    public void whenBought(double signedShares) {
        totalMarketShares += signedShares;
    }

    @Override
    public void saveInFile(ConfigurationSection config) {
        config.set("initial-supply", initialMarketShares);
        config.set("total-supply", totalMarketShares);
        config.set("price-multiplier", priceMultiplier);
    }

    /**
     * @return The price the stock should have at any moment
     */
    public double computePrice(double totalShares) {
        return priceMultiplier * (totalShares < initialMarketShares / 10 ? expBehaviour(totalShares) : totalShares);
    }

    private double expBehaviour(double totalSup) {
        return initialMarketShares / 10 * (Math.exp(totalSup - (initialMarketShares / 10)));
    }

    @Override
    public double getShareInitialPrice(double signedShares) {
        return computePrice(totalMarketShares + signedShares);
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
        priceMultiplier *= 1 + (RANDOM.nextDouble() - 0.5) * Stonks.plugin.configManager.volatility * Math.sqrt(stock.getRefreshPeriod()) /
                (Math.sqrt(10 * TimeScale.HOUR.getTime()));
    }
}
