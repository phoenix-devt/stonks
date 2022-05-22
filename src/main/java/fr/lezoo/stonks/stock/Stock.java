package fr.lezoo.stonks.stock;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.stock.handler.FictiveStockHandler;
import fr.lezoo.stonks.stock.handler.RealStockHandler;
import fr.lezoo.stonks.stock.handler.StockHandler;
import fr.lezoo.stonks.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

/**
 * Place where players can buy and sell shares
 */
public class Stock {
    protected final String id, name;

    @NotNull
    private Dividends dividends;

    /**
     * The material that will be exchanged. If this is set to null,
     * that means the stock is virtual and exchanges money instead
     */
    @Nullable
    private final ExchangeType exchangeType;


    @NotNull
    private final StockHandler handler;

    /**
     * List of data for every scale. Allows to store just the right
     * amount of data needed so that there aren't 10s timestamps on the yearly scale.
     */
    protected final Map<TimeScale, List<StockInfo>> stockData = new HashMap<>();

    /**
     * How frequently this stock refreshes in ticks!!
     */
    private final long refreshPeriod;

    /**
     * Amount of points of historical stock data
     * kept in cache on one specific time scale.
     */
    public static final int BOARD_DATA_NUMBER = 100;

    private static final long REAL_STOCK_DEFAULT_REFRESH_PERIOD = TimeScale.HOUR.getTime() / (BOARD_DATA_NUMBER * 50);
    private static final long VIRTUAL_STOCK_DEFAULT_REFRESH_PERIOD = TimeScale.MINUTE.getTime() / (BOARD_DATA_NUMBER * 50);


    /**
     * Public constructor to create a new Stock from scratch
     *
     * @param id                 Internal stock id
     * @param name               Name of the stock
     * @param dividends          Whether or not this stocks gives dividends to investors
     * @param exchangeType       The material being exchanged, or null if the stock is virtual
     * @param firstStockData The only StockInfo that exists
     */
    public Stock(String id, String name, Function<Stock, StockHandler> handlerProvider, Dividends dividends, @Nullable ExchangeType exchangeType, StockInfo firstStockData) {
        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = name;
        this.dividends = dividends;
        this.exchangeType = exchangeType;
        for (TimeScale disp : TimeScale.values())
            stockData.put(disp, Arrays.asList(firstStockData));
        Stonks.plugin.stockManager.initializeStockData(this);
        //Handler provider needs to be set up in last
        this.handler = handlerProvider.apply(this);
        this.refreshPeriod =handler instanceof RealStockHandler? REAL_STOCK_DEFAULT_REFRESH_PERIOD:VIRTUAL_STOCK_DEFAULT_REFRESH_PERIOD;


    }

    /**
     * Loads a stock from a config section
     */
    public Stock(ConfigurationSection config) {
        this.id = config.getName();
        this.name = config.getString("name");

        // If it doesn't have a field dividends we use the default dividends given in the config.yml
        this.dividends = config.contains("dividends") ? new Dividends(this, config.getConfigurationSection("dividends")) : new Dividends(this);


        exchangeType = config.contains("exchange-type") ? new ExchangeType(config.getConfigurationSection("exchange-type")) : null;
        // Set the data of the stock
        Stonks.plugin.stockManager.initializeStockData(this);
        //Handler provider needs to be set up in last
        this.handler = config.getBoolean("real-stock") ? new RealStockHandler(this) : new FictiveStockHandler(this, config);
        this.refreshPeriod = config.getLong("refresh-period", config.getBoolean("real-stock") ?REAL_STOCK_DEFAULT_REFRESH_PERIOD:VIRTUAL_STOCK_DEFAULT_REFRESH_PERIOD);

    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean hasDividends() {
        return dividends != null;
    }

    public Dividends getDividends() {
        return dividends;
    }

    public void setDividends(Dividends dividends) {
        this.dividends = dividends;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public boolean isVirtual() {
        return exchangeType == null;
    }

    public List<StockInfo> getData(TimeScale disp) {
        return stockData.get(disp);
    }

    public boolean isRealStock() {
        return handler instanceof RealStockHandler;
    }

    public StockHandler getHandler() {
        return handler;
    }

    public long getRefreshPeriod() {
        return refreshPeriod;
    }

    /**
     * Updates the attributes of Stock regarding on the time given
     *
     * @param time          the time corresponding to the data
     * @param stockData the data we want to update
     */
    public void setData(TimeScale time, List<StockInfo> stockData) {
        this.stockData.put(time, stockData);
    }

    /**
     * This method compares the current price with the info which time
     * stamp matches the most the time stamp given as parameter
     *
     * @param time Difference of time in the past, in millis
     * @return Growth rate compared to some time ago
     */
    public double getEvolution(TimeScale time) {
        List<StockInfo> stockData = this.getData(time);

        /*
         * Last information in the list corresponds to the latest information.
         * First information corresponds to the oldest, which gives us the growth rate.
         *
         * We need a division by zero check?
         */
        double oldest = stockData.get(0).getPrice();
        double latest = getPrice();

        return Utils.truncate(100 * (latest - oldest) / oldest, 1);
    }

    public void save(FileConfiguration config) {

        // If the stock is empty we destroy it to not overload memory and avoid errors
        if (stockData.get(TimeScale.HOUR) == null || stockData.get(TimeScale.HOUR).size() == 0) {
            config.set(id, null);
            return;
        }
        if (!config.contains(id + ".name"))
            config.set(id + ".name", name);
        handler.saveInFile(config.getConfigurationSection(id));
        config.set(id + ".real-stock", handler instanceof RealStockHandler);

        if (!config.contains(id + ".refresh-period"))
            config.set(id + ".refresh-period", refreshPeriod);

        // If the stock has dividends we save it
        if (hasDividends()) {
            if (!config.contains(id + ".dividends.formula"))
                config.set(id + ".dividends.formula", dividends.getFormula());
            if (!config.contains(id + ".dividends.period"))
                config.set(id + ".dividends.period", dividends.getPeriod());
            config.set(id + ".dividends.last", dividends.getLastApplication());
        }

        // Save exchange type
        if (isVirtual())
            config.set(id + ".exchange-type", null);
        else {
            config.set(id + ".exchange-type.material", exchangeType.getMaterial().name());
            config.set(id + ".exchange-type.model-data", exchangeType.getModelData());
        }

        Stonks.plugin.stockManager.save(this);

    }

    /**
     * @param time The time we want to look back for the stock
     * @return Lowest price for the given time
     */
    public double getLowest(TimeScale time) {
        List<StockInfo> stockData = this.getData(time);
        if (stockData.size() == 0)
            Stonks.plugin.getLogger().log(Level.WARNING, "Can't get lowest value of stock '" + id + "' as data is empty");

        double min = stockData.get(0).getPrice();
        for (StockInfo stockInfo : stockData)
            if (stockInfo.getPrice() < min)
                min = stockInfo.getPrice();
        return min;
    }

    /**
     * @param time The time we want to look back for the stock
     * @return Highest price for the given time
     */
    public double getHighest(TimeScale time) {
        List<StockInfo> stockData = this.getData(time);
        if (stockData.size() == 0)
            Stonks.plugin.getLogger().log(Level.WARNING, "Can't get highest value of stock '" + id + "' as data is empty");

        double max = stockData.get(0).getPrice();
        for (StockInfo stockInfo : stockData)
            if (stockInfo.getPrice() > max)
                max = stockInfo.getPrice();
        return max;
    }

    /**
     * @return Current stock price
     */
    public double getPrice() {
        return handler.getCurrentPrice();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stock stock = (Stock) o;
        return id.equals(stock.id) && name.equals(stock.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
