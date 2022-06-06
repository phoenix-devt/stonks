package fr.lezoo.stonks.stock.handler;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.TimeScale;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class RealStockHandler implements StockHandler {
    private final Stock stock;

    /**
     * Last price in history
     */
    private StockInfo lastStockInfo;

    public RealStockHandler(Stock stock) {
        this.stock = stock;
    }

    @Override
    public void whenBought(ShareType type, double signedShares) {
        // Nothing happens
    }

    @Override
    public void saveInFile(ConfigurationSection config) {
        // Nothing happens
    }

    @Override
    public double getCurrentPrice() {

        if (lastStockInfo == null) {
            final List<StockInfo> latestInfo = stock.getData(TimeScale.MINUTE);
            Validate.isTrue(!latestInfo.isEmpty(), "No stock data found for '" + stock.getId() + "'");
            return latestInfo.get(latestInfo.size() - 1).getPrice();
        }

        return lastStockInfo.getPrice();
    }

    @Override
    public double getSellPrice(Share share) {
        return stock.getPrice();
    }

    @Override
    public void refreshPrice() {
        Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {
            try {
                final double lastPrice = Stonks.plugin.stockAPI.getPrice(stock.getId());
                lastStockInfo = new StockInfo(System.currentTimeMillis(), lastPrice);

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
