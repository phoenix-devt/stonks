package fr.lezoo.stonks.quotation;

import fr.lezoo.stonks.Stonks;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Random;

/**
 * Quotation which is not based on any
 */
public class CreatedQuotation extends Quotation {
    private double previousDemand, currentDemand;

    public CreatedQuotation(String id, String name, Dividends dividends, QuotationInfo firstQuotationData) {
        super(id, name, dividends, firstQuotationData);
    }

    public CreatedQuotation(ConfigurationSection config) {
        super(config);
    }

    /**
     * Changes the quotation price
     */
    public void refreshQuotation() {
        if (getData(QuotationTimeDisplay.QUARTERHOUR).isEmpty())
            return;

        Random random = new Random();
        double rand = random.nextInt(2);
        double change = rand == 0 ? -1 : 1;
        // The offset due to the offer and demand of the stock
        double offset = Stonks.plugin.configManager.offerDemandImpact * Math.atan(previousDemand) * 2 / Math.PI;
        double currentPrice = getPrice();
        //The change between the currentPrice and nextPrice

        //We multiply by sqrt(t) so that volatility doesn't depend on refreshTime

        change = (change + offset) * Stonks.plugin.configManager.volatility * currentPrice / 20 * Math.sqrt((double) (Stonks.plugin.configManager.quotationRefreshTime) / 3600);

        int datanumber = Stonks.plugin.configManager.quotationDataNumber;
        //We update all the data List
        for (QuotationTimeDisplay time : QuotationTimeDisplay.values()) {
            //We get the list corresponding to the time
            List<QuotationInfo> workingData = this.getData(time);
            //If the the latest data of workingData is too old we add another one
            if (System.currentTimeMillis() - workingData.get(workingData.size() - 1).getTimeStamp() > time.getTime() / datanumber) {


                workingData.add(new QuotationInfo(System.currentTimeMillis(), currentPrice + change));
                //If the list contains too much data we remove the older ones
                if (workingData.size() > datanumber)
                    workingData.remove(0);
                //We save the changes we made in the attribute
                this.setData(time, workingData);
            }


        }

    }


    public void addDemand(double stockBought) {
        currentDemand += stockBought;
    }
}
