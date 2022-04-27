package fr.lezoo.stonks.quotation;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.handler.FictiveStockHandler;
import fr.lezoo.stonks.quotation.handler.RealStockHandler;
import fr.lezoo.stonks.quotation.handler.StockHandler;
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
public class Quotation {
    protected final String id, name;

    @NotNull
    private Dividends dividends;

    /**
     * The material that will be exchanged. If this is set to null,
     * that means the quotation is virtual and exchanges money instead
     */
    @Nullable
    private final ExchangeType exchangeType;


    @NotNull
    private final StockHandler handler;

    /**
     * List of data for every scale. Allows to store just the right
     * amount of data needed so that there aren't 10s timestamps on the yearly scale.
     */
    protected final Map<TimeScale, List<QuotationInfo>> quotationData = new HashMap<>();

    /**
     * How frequently this quotation refreshes in seconds
     */
    private final long refreshPeriod;

    /**
     * Amount of points of historical stock data
     * kept in cache on one specific time scale.
     */
    public static final int BOARD_DATA_NUMBER = 100;

    private static final long DEFAULT_REFRESH_PERIOD = TimeScale.HOUR.getTime() / BOARD_DATA_NUMBER / 1000;

    /**
     * Public constructor to create a new Quotation from scratch
     *
     * @param id                 Internal quotation id
     * @param name               Name of the stock
     * @param dividends          Whether or not this quotations gives dividends to investors
     * @param exchangeType       The material being exchanged, or null if the quotation is virtual
     * @param firstQuotationData The only QuotationInfo that exists
     */
    public Quotation(String id, String name, Function<Quotation, StockHandler> handlerProvider, Dividends dividends, @Nullable ExchangeType exchangeType, QuotationInfo firstQuotationData) {
        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = name;
        this.dividends = dividends;
        this.exchangeType = exchangeType;
        for (TimeScale disp : TimeScale.values())
            quotationData.put(disp, Arrays.asList(firstQuotationData));
        this.refreshPeriod = DEFAULT_REFRESH_PERIOD;
        Stonks.plugin.quotationManager.initializeQuotationData(this);
        //Handler provider needs to be set up in last
        this.handler = handlerProvider.apply(this);

    }

    /**
     * Loads a quotation from a config section
     */
    public Quotation(ConfigurationSection config) {
        this.id = config.getName();
        this.name = config.getString("name");

        // If it doesn't have a field dividends we use the default dividends given in the config.yml
        this.dividends = config.contains("dividends") ? new Dividends(this, config.getConfigurationSection("dividends")) : new Dividends(this);
        this.refreshPeriod = config.getLong("refresh-period", DEFAULT_REFRESH_PERIOD);


        exchangeType = config.contains("exchange-type") ? new ExchangeType(config.getConfigurationSection("exchange-type")) : null;
        // Set the data of the quotation
        Stonks.plugin.quotationManager.initializeQuotationData(this);
        //Handler provider needs to be set up in last
        this.handler = config.getBoolean("real-stock") ? new RealStockHandler(this) : new FictiveStockHandler(this, config);

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

    public List<QuotationInfo> getData(TimeScale disp) {
        return quotationData.get(disp);
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
     * Updates the attributes of Quotation regarding on the time given
     *
     * @param time          the time corresponding to the data
     * @param quotationData the data we want to update
     */
    public void setData(TimeScale time, List<QuotationInfo> quotationData) {
        this.quotationData.put(time, quotationData);
    }

    /**
     * This method compares the current price with the info which time
     * stamp matches the most the time stamp given as parameter
     *
     * @param time Difference of time in the past, in millis
     * @return Growth rate compared to some time ago
     */
    public double getEvolution(TimeScale time) {
        List<QuotationInfo> quotationData = this.getData(time);

        /*
         * Last information in the list corresponds to the latest information.
         * First information corresponds to the oldest, which gives us the growth rate.
         *
         * We need a division by zero check?
         */
        double oldest = quotationData.get(0).getPrice();
        double latest = getPrice();

        return Utils.truncate(100 * (latest - oldest) / oldest, 1);
    }

    public void save(FileConfiguration config) {

        // If the quotation is empty we destroy it to not overload memory and avoid errors
        if (quotationData.get(TimeScale.HOUR) == null || quotationData.get(TimeScale.HOUR).size() == 0) {
            config.set(id, null);
            return;
        }

        config.set(id + ".name", name);
        handler.saveInFile(config.getConfigurationSection(id));
        config.set(id + ".real-stock", handler instanceof RealStockHandler);
        config.set(id + ".refresh-period", refreshPeriod);

        // If the quotation has dividends we save it
        if (hasDividends()) {
            config.set(id + ".dividends.formula", dividends.getFormula());
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

        Stonks.plugin.quotationManager.save(this);

    }

    /**
     * @param time The time we want to look back for the quotation
     * @return Lowest price for the given time
     */
    public double getLowest(TimeScale time) {
        List<QuotationInfo> quotationData = this.getData(time);
        if (quotationData.size() == 0)
            Stonks.plugin.getLogger().log(Level.WARNING, "Can't get lowest value of quotation '" + id + "' as data is empty");

        double min = quotationData.get(0).getPrice();
        for (QuotationInfo quotationInfo : quotationData)
            if (quotationInfo.getPrice() < min)
                min = quotationInfo.getPrice();
        return min;
    }

    /**
     * @param time The time we want to look back for the quotation
     * @return Highest price for the given time
     */
    public double getHighest(TimeScale time) {
        List<QuotationInfo> quotationData = this.getData(time);
        if (quotationData.size() == 0)
            Stonks.plugin.getLogger().log(Level.WARNING, "Can't get highest value of quotation '" + id + "' as data is empty");

        double max = quotationData.get(0).getPrice();
        for (QuotationInfo quotationInfo : quotationData)
            if (quotationInfo.getPrice() > max)
                max = quotationInfo.getPrice();
        return max;
    }

    /**
     * @return Current quotation price
     */
    public double getPrice() {
        List<QuotationInfo> latest = quotationData.get(TimeScale.HOUR);
        return latest.get(latest.size() - 1).getPrice();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quotation quotation = (Quotation) o;
        return id.equals(quotation.id) && name.equals(quotation.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
