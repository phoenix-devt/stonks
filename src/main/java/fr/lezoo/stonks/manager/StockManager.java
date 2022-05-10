package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
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
        Validate.isTrue(mapped.containsKey(stockId), "Tried to remove stock " + stockId + " which does not exist");

        LoadedStock removed = mapped.remove(stockId);
        removed.refreshRunnable.cancel();
    }

    /**
     * Set the data correponding to the stocks. But it doesn't refresh the price or try to get it.
     * The refreshPrice method is here to get an updated version of the price
     */
    public void refresh() {
        for(LoadedStock stock:mapped.values()) {
            stock.stock.getHandler().refresh();
        }
    }

    @Override
    public void save() {
        ConfigFile config = new ConfigFile("stocks");

        //Remove the data of the stocks in stock-data.yml
        ConfigFile stockDataConfig = new ConfigFile("stock-data");
        for (String key : stockDataConfig.getConfig().getKeys(true))
            stockDataConfig.getConfig().set(key, null);

        // Save newest
        for (LoadedStock loaded : mapped.values())
            loaded.stock.save(config.getConfig());

        config.save();
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
                }
            };
            refreshRunnable.runTaskTimer(Stonks.plugin, 20, stock.getRefreshPeriod());
        }
    }

    /**
     * set the stock data for a stock when StockManager loads
     *
     * @param stock The stock which data needs to be set
     */
    public void initializeStockData(Stock stock) {
        // We load the different data from the yml if they exist
        if (new ConfigFile("stock-data").getConfig().getKeys(false).contains(stock.getId())) {

            ConfigurationSection section = new ConfigFile("stock-data").getConfig().getConfigurationSection(stock.getId());


            for (TimeScale time : TimeScale.values()) {
                int i = 0;
                List<StockInfo> workingStock = new ArrayList<>();

                while (section.contains(time.toString().toLowerCase() + "data." + i)) {
                    workingStock.add(new StockInfo(section.getConfigurationSection(time.toString().toLowerCase() + "data." + i)));
                    i++;
                }

                // We change the attribute
                stock.setData(time, workingStock);

            }
        }
        //Otherwise we create the firstStockData depending on the stock type
        else {


            if (stock.getHandler() instanceof RealStockHandler) {

                Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {
                    try {
                        double price = Stonks.plugin.stockAPI.getPrice(stock.getId());
                        StockInfo firstStockData = new StockInfo(System.currentTimeMillis(), price);
                        for (TimeScale disp : TimeScale.values())
                            stock.setData(disp, Arrays.asList(firstStockData));

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                });

            }
            //If it a virtual Stock we set the initial price at 10 if there is no data for the stock
            else {
                for (TimeScale disp : TimeScale.values())
                    if (stock.getData(disp).size() == 0)
                        stock.setData(disp, Arrays.asList(new StockInfo(System.currentTimeMillis(), 10)));

            }
        }

    }


    /**
     * Saves all the data of the stocks in stock-data.yml file
     */
    public void save(Stock stock) {
        ConfigFile configFile = new ConfigFile("stock-data");
        FileConfiguration config = configFile.getConfig();
        //We remove the old data
        if (config.contains(stock.getId())) {
            ConfigurationSection section = config.getConfigurationSection(stock.getId());
            for (String key : section.getKeys(true)) {
                section.set(key, null);

            }
        }
        //We save the information of the data using stockDataManager
        for (TimeScale time : TimeScale.values()) {
            List<StockInfo> stockData = stock.getData(time);
            //We load the data needed
            for (int i = 0; i < stockData.size(); i++) {
                config.set(stock.getId() + "." + time.toString().toLowerCase() + "data." + i + ".price", stockData.get(i).getPrice());
                config.set(stock.getId() + "." + time.toString().toLowerCase() + "data." + i + ".timestamp", stockData.get(i).getTimeStamp());
            }
        }
        configFile.save();
    }
}
