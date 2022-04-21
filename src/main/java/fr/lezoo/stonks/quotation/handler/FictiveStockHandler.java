package fr.lezoo.stonks.quotation.handler;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.TimeScale;
import org.apache.commons.lang.NotImplementedException;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FictiveStockHandler implements StockHandler {
    private final Quotation quotation;

    private double previousDemand, currentDemand;

    public FictiveStockHandler(Quotation quotation) {
        this.quotation = quotation;
    }

    public FictiveStockHandler(Quotation quotation, ConfigurationSection config) {
        this.quotation = quotation;

        previousDemand = config.getDouble("previous-demand");
        currentDemand = config.getDouble("current-demand");
    }

    @Override
    public void whenBought(double stocksBought) {
        throw new NotImplementedException();
    }

    @Override
    public void saveInFile(ConfigurationSection config) {
        config.set("previous-demand", previousDemand);
        config.set("current-demand", currentDemand);
    }

    @Override
    public void refresh() {

        if (quotation.getData(TimeScale.HOUR).isEmpty())
            return;

        Random random = new Random();
        double change = random.nextInt(2) == 0 ? -1 : 1;
        // The offset due to the offer and demand of the stock
        double offset = Stonks.plugin.configManager.offerDemandImpact * Math.atan(previousDemand) * 2 / Math.PI;
        double currentPrice = quotation.getPrice();
        // The change between the currentPrice and nextPrice

        // We multiply by sqrt(t) so that volatility doesn't depend on refreshTime

        change = (change + offset) * Stonks.plugin.configManager.volatility * currentPrice / 20 * Math.sqrt((double) (Stonks.plugin.configManager.quotationRefreshTime) / 3600);

        // The amount of data wanted for each timescale fo the quotation
        int datanumber = Stonks.plugin.configManager.quotationDataNumber;
        // We update all the data List
        for (TimeScale time : TimeScale.values()) {
            // We get the list corresponding to the time
            List<QuotationInfo> workingData = new ArrayList<>();
            workingData.addAll(quotation.getData(time));
            // If the the latest data of workingData is too old we add another one
            if (System.currentTimeMillis() - workingData.get(workingData.size() - 1).getTimeStamp() > time.getTime() / datanumber) {

                workingData.add(new QuotationInfo(System.currentTimeMillis(), currentPrice + change));
                // If the list contains too much data we remove the older ones
                if (workingData.size() > datanumber)
                    workingData.remove(0);
                // We save the changes we made in the attribute
                quotation.setData(time, workingData);
            }
        }
    }

    public void addDemand(double stockBought) {
        currentDemand += stockBought;
    }
}
