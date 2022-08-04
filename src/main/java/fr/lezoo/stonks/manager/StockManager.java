package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.StockPriceUpdateEvent;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.TimeScale;
import fr.lezoo.stonks.stock.handler.RealStockHandler;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class StockManager implements FileManager {
    private final Map<String, LoadedStock> mapped = new HashMap<>();

    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("stocks").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Stock(config.getConfigurationSection(key)));
            } catch (RuntimeException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load stock '" + key + "': " + exception.getMessage());
            }
    }

    public void remove(String stockId) {
        final @Nullable LoadedStock removed = mapped.remove(stockId);
        Validate.notNull(removed, "Tried removing stock '" + stockId + "' which does not exist");

        // Stock was found and gets removed
        removed.refreshRunnable.cancel();

        // Remove it from the yml
        ConfigFile config = new ConfigFile("stocks");
        config.getConfig().set(stockId, null);
        config.save();
    }

    /**
     * Set the data correponding to the stocks. But it doesn't refresh the price or try to get it.
     * The refreshPrice method is here to get an updated version of the price
     */
    public void refresh() {
        for (LoadedStock stock : mapped.values())
            stock.stock.saveCurrentStateAsStockData();
    }

    @Override
    public void save() {
        ConfigFile stockInfoConfig = new ConfigFile("stocks");
        ConfigFile stockDataConfig = new ConfigFile("stock-data");

        // Remove old stock data
        for (String key : stockDataConfig.getConfig().getKeys(true))
            stockDataConfig.getConfig().set(key, null);

        // Save newest
        for (LoadedStock loaded : mapped.values())
            loaded.stock.save(stockInfoConfig.getConfig(), stockDataConfig.getConfig());

        stockInfoConfig.save();
        stockDataConfig.save();
    }

    public boolean has(String id) {
        return mapped.containsKey(formatId(id));
    }

    /**
     * Gets the stock with corresponding ID, or throws an IAE
     *
     * @param id Stock identifier
     * @return Corresponding stock
     */
    @NotNull
    public Stock get(String id) {
        Validate.isTrue(mapped.containsKey(formatId(id)), "No stock found with ID '" + formatId(id) + "'");
        return mapped.get(formatId(id)).stock;
    }

    public void register(Stock stock) {
        Validate.isTrue(!mapped.containsKey(stock.getId()), "There is already a stock with ID " + stock.getId() + "'");
        mapped.put(stock.getId(), new LoadedStock(stock));
    }

    public void forEachStock(Consumer<Stock> action) {
        for (LoadedStock loaded : mapped.values())
            action.accept(loaded.stock);
    }

    public Set<Stock> getStocks() {
        Set<Stock> set = new HashSet<>();
        for (LoadedStock loaded : mapped.values())
            set.add(loaded.stock);
        return set;
    }

    private String formatId(String str) {
        return str.toLowerCase().replace(" ", "-").replace("_", "-");
    }

    class LoadedStock {
        final Stock stock;
        final BukkitRunnable refreshRunnable;

        LoadedStock(Stock stock) {
            this.stock = stock;
            this.refreshRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    stock.getHandler().refreshPrice();
                    Bukkit.getPluginManager().callEvent(new StockPriceUpdateEvent(stock));
                }
            };
            refreshRunnable.runTaskTimer(Stonks.plugin, 20, stock.getRefreshPeriod());
        }
    }

    /**
     * set the stock data for a stock when StockManager loads
     *
     * @param stock The stock which data needs to be initialized
     */
    public void initializeStockData(Stock stock) {

        // Load the different data from the yml if they exist
        FileConfiguration stockData = new ConfigFile("stock-data").getConfig();
        if (stockData.contains(stock.getId())) {
            ConfigurationSection section = new ConfigFile("stock-data").getConfig().getConfigurationSection(stock.getId());

            for (TimeScale time : TimeScale.values()) {
                int i = 0;
                List<StockInfo> workingStock = stock.getData(time);
                while (section.contains(time.toString().toLowerCase() + "data." + i)) {
                    workingStock.add(new StockInfo(section.getConfigurationSection(time.toString().toLowerCase() + "data." + i)));
                    i++;
                }
            }
        }

        // Otherwise we create the first stock data depending on stock type
        else {
            if (stock.isRealStock()) {
                Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {
                    try {
                        double price = Stonks.plugin.stockAPI.getPrice(stock.getId());
                        StockInfo firstStockData = new StockInfo(System.currentTimeMillis(), price);
                        for (TimeScale disp : TimeScale.values())
                            stock.getData(disp).add(firstStockData);

                    } catch (URISyntaxException | IOException | InterruptedException | ParseException e) {
                        e.printStackTrace();
                    }
                });
            }

            // If it is a virtual stock
            else
                for (TimeScale disp : TimeScale.values())
                    if (stock.getData(disp).isEmpty())
                        stock.getData(disp).add(new StockInfo(System.currentTimeMillis(), stock.getPrice()));
        }
    }
}
