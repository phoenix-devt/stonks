package fr.lezoo.stonks.stock.handler;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.TimeScale;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RealStockHandler implements StockHandler {
    private final Stock stock;
    private double lastPrice;


    public RealStockHandler(Stock stock) {
        this.stock = stock;
    }

    @Override
    public void whenBought(double stocksBought) {
        // Nothing happens
    }

    @Override
    public void saveInFile(ConfigurationSection config) {
        // Nothing happens
    }

    @Override
    public void refresh() {
        //We update all the data List
        for (TimeScale time : TimeScale.values()) {
            //We get the list corresponding to the time
            List<StockInfo> workingData = new ArrayList<>();
            Validate.notNull(stock.getData(time), "The data for " + stock.getId() + " for " + time.toString() + " is null");
            workingData.addAll(stock.getData(time));
            //If the the latest data of workingData is too old we add another one
            if (System.currentTimeMillis() - workingData.get(workingData.size() - 1).getTimeStamp() > time.getTime() / Stock.BOARD_DATA_NUMBER) {
                workingData.add(new StockInfo(System.currentTimeMillis(), lastPrice));
                //If the list contains too much data we remove the older ones
                if (workingData.size() > Stock.BOARD_DATA_NUMBER)
                    workingData.remove(0);
                //We save the changes we made in the attribute
                stock.setData(time, workingData);
            }
        }
    }

    @Override
    public void refreshPrice() {
        Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {
            try {
                lastPrice = Stonks.plugin.stockAPI.getPrice(stock.getId());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        });

    }
}
