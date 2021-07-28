package fr.lezoo.stonks.api.quotation;

import fr.lezoo.stonks.Stonks;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Random;

/**
 * Quotation which handles Vault economy
 */
public class VirtualQuotation extends Quotation {
    private double previousDemand = 0;
    private double currentDemand = 0;


    public VirtualQuotation(String id, String companyName, String stockName, Dividends dividends, List<QuotationInfo> quotationData) {
        super(id, companyName, stockName, dividends, quotationData);
    }

    public VirtualQuotation(ConfigurationSection config) {
        super(config);
    }

    public VirtualQuotation(String id, String companyName, String stockName, Dividends dividends) {
        super(id, companyName, stockName, dividends);
    }

    /**
     * Changes the quotatinon price
     */
    public void refreshQuotation() {

        if (quotationData.size() == 0)
            return;
        Random random = new Random();
        double change = random.nextInt(2) == 0 ? -1 : 1;
        // The offset due to the offer and demand of the stock
        double offset = Stonks.plugin.configManager.offerDemandImpact * Math.atan(previousDemand) * 2 / Math.PI;

        double currentPrice = quotationData.get(quotationData.size() - 1).getPrice();
        //The change between the currentPrice and nextPrice
        //With max volatily it can change in the worst case by 10% in 1 hour
        //We multiply by sqrt(t) so that volatility doesn't depend on refreshTime
        change = (change + offset) * Stonks.plugin.configManager.volatility * currentPrice / 20 * Math.sqrt((Stonks.plugin.configManager.quotationRefreshTime) / 3600);
        quotationData.remove(0);
        quotationData.add(new QuotationInfo(System.currentTimeMillis(), currentPrice + change));

    }


    public void addDemand(double stockBought) {
        currentDemand += stockBought;
    }
}
