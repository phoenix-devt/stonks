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
     * @param id             Internal stock id
     * @param name           Name of the stock
     * @param dividends      Whether or not this stocks gives dividends to investors
     * @param exchangeType   The material being exchanged, or null if the stock is virtual
     * @param firstStockData The only StockInfo that exists
     */
    public Stock(String id, String name, Function<Stock, StockHandler> handlerProvider, Dividends dividends, @Nullable ExchangeType exchangeType, StockInfo firstStockData) {
        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = name;
        this.dividends = dividends;
        this.exchangeType = exchangeType;
        for (TimeScale disp : TimeScale.values())
            stockData.put(disp, new ArrayList(Arrays.asList(firstStockData)));
        Stonks.plugin.stockManager.initializeStockData(this);
        // Handler provider needs to be set up in last
        this.handler = handlerProvider.apply(this);
        this.refreshPeriod = handler instanceof RealStockHandler ? REAL_STOCK_DEFAULT_REFRESH_PERIOD : VIRTUAL_STOCK_DEFAULT_REFRESH_PERIOD;
    }

    /**
     * Loads a stock from a config section
     */
    public Stock(ConfigurationSection config) {
        this.id = config.getName();
        this.name = config.getString("name");

        // If it doesn't have a field dividends we use the default dividends given in the config.yml
        this.dividends = config.contains("dividends") ? new Dividends(this, config.getConfigurationSection("dividends")) : new Dividends(this);

        exchangeType = config.contains("exchange-type")&&!config.getString("exchange-type").equals("money") ? new ExchangeType(config.getConfigurationSection("exchange-type")) : null;
        this.handler = config.getBoolean("real-stock") ? new RealStockHandler(this) : new FictiveStockHandler(this, config);
        // Set the data of the stock after initializing stock handler
        Stonks.plugin.stockManager.initializeStockData(this);
        this.refreshPeriod = config.getLong("refresh-period", config.getBoolean("real-stock") ? REAL_STOCK_DEFAULT_REFRESH_PERIOD : VIRTUAL_STOCK_DEFAULT_REFRESH_PERIOD);
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

    /**
     * @return Returned object is NOT a clone
     */
    @NotNull
    public List<StockInfo> getData(TimeScale disp) {
        List<StockInfo> found = stockData.get(disp);
        if (found != null)
            return found;

        found = new ArrayList<>();
        stockData.put(disp, found);
        return found;
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

    /**
     * We change the stock data only if it is not in the config so that we take in account the information changes in the yml.
     */
    public void save(FileConfiguration stockInfo, FileConfiguration stockData) {

        // If the stock is empty we destroy it to not overload memory and avoid errors
        if (this.stockData.get(TimeScale.HOUR) == null || this.stockData.get(TimeScale.HOUR).size() == 0) {
            stockInfo.set(id, null);
            return;
        }

        // Save main stock info
        if (!stockInfo.contains(id + ".name"))
            stockInfo.set(id + ".name", name);
        handler.saveInFile(stockInfo.getConfigurationSection(id));
        stockInfo.set(id + ".real-stock", handler instanceof RealStockHandler);

        if (!stockInfo.contains(id + ".refresh-period"))
            stockInfo.set(id + ".refresh-period", refreshPeriod);

        // If the stock has dividends we save it
        if (hasDividends()) {
            if (!stockInfo.contains(id + ".dividends.formula"))
                stockInfo.set(id + ".dividends.formula", dividends.getFormula());
            if (!stockInfo.contains(id + ".dividends.period"))
                stockInfo.set(id + ".dividends.period", dividends.getPeriod());
            if (!stockInfo.contains(id + ".dividends.last") || dividends.getLastApplication() > stockInfo.getLong(id + ".dividends.last"))
                stockInfo.set(id + ".dividends.last", dividends.getLastApplication());
        }
        if (!stockInfo.contains(id + ".exchange-type")) {
            // Save exchange type
            if (isVirtual())
                stockInfo.set(id + ".exchange-type", "money");
            else {
                stockInfo.set(id + ".exchange-type.material", exchangeType.getMaterial().name());
                stockInfo.set(id + ".exchange-type.model-data", exchangeType.getModelData());
                stockInfo.set(id + ".exchange-type.display", exchangeType.getDisplay());
            }
        }

        // Save stock data
        for (TimeScale time : TimeScale.values()) {
            List<StockInfo> toBeSaved = getData(time);
            for (int i = 0; i < toBeSaved.size(); i++) {
                stockData.set(id + "." + time.toString().toLowerCase() + "data." + i + ".price", toBeSaved.get(i).getPrice());
                stockData.set(id + "." + time.toString().toLowerCase() + "data." + i + ".timestamp", toBeSaved.get(i).getTimeStamp());
            }
        }
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

    public void saveCurrentStateAsStockData() {

        // Compute current price
        double price = handler.getCurrentPrice();

        for (TimeScale time : TimeScale.values()) {

            // Find mutable list of stock data
            List<StockInfo> workingData = getData(time);

            // This fixes an issue with empty working data
            long lastTimeStamp = workingData.isEmpty() ? 0 : workingData.get(workingData.size() - 1).getTimeStamp();

            // If the the latest data FOR THAT TIME SCALE is too old, ADD a new one
            if (System.currentTimeMillis() - lastTimeStamp > time.getTime() / Stock.BOARD_DATA_NUMBER) {
                workingData.add(new StockInfo(System.currentTimeMillis(), price));

                // If the list contains too much data we remove the older ones
                if (workingData.size() > Stock.BOARD_DATA_NUMBER)
                    workingData.remove(0);
            }
        }
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
