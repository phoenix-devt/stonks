package fr.lezoo.stonks.quotation.handler;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.TimeScale;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FictiveStockHandler implements StockHandler {
    private final Quotation quotation;

    private double previousDemand, currentDemand;

    private static final Random RANDOM = new Random();

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
        currentDemand += stocksBought;
    }

    @Override
    public void saveInFile(ConfigurationSection config) {
        config.set("previous-demand", previousDemand);
        config.set("current-demand", currentDemand);
    }

    // TODO fix formula + current/previous demand
    @Override
    public void refresh() {

        if (quotation.getData(TimeScale.HOUR).isEmpty())
            return;

        double change = RANDOM.nextBoolean() ? -1 : 1;
        // The offset due to the offer and demand of the stock
        double offset = Stonks.plugin.configManager.offerDemandImpact * Math.atan(previousDemand) * 2 / Math.PI;
        double currentPrice = quotation.getPrice();
        // The change between the currentPrice and nextPrice

        // We multiply by sqrt(t) so that volatility doesn't depend on refreshTime

        change = (change + offset) * Stonks.plugin.configManager.volatility * currentPrice / 20 * Math.sqrt(quotation.getRefreshPeriod());

        // The amount of data wanted for each timescale fo the quotation
        // We update all the data List
        for (TimeScale time : TimeScale.values()) {

            // We get the list corresponding to the time
            List<QuotationInfo> workingData = new ArrayList<>();
            workingData.addAll(quotation.getData(time));

            // This fixes an issue with empty working data
            long lastTimeStamp = workingData.isEmpty() ? 0 : workingData.get(workingData.size() - 1).getTimeStamp();

            // If the the latest data of workingData is too old we add another one
            if (System.currentTimeMillis() - lastTimeStamp > time.getTime() / Quotation.BOARD_DATA_NUMBER) {

                workingData.add(new QuotationInfo(System.currentTimeMillis(), currentPrice + change));
                // If the list contains too much data we remove the older ones
                if (workingData.size() > Quotation.BOARD_DATA_NUMBER)
                    workingData.remove(0);
                // We save the changes we made in the attribute
                quotation.setData(time, workingData);
            }
        }
    }
}
