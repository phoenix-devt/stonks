package fr.lezoo.stonks.quotation;

import fr.lezoo.stonks.Stonks;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is like a normal Quotation but reflects the price of a real existing quotation
 */
public class RealStockQuotation extends Quotation {

    public RealStockQuotation(String id, String name, QuotationInfo firstQuotationData) {
        super(id, name, firstQuotationData);
    }

    public RealStockQuotation(String id, String name, ExchangeType exchangeType, QuotationInfo firstQuotationData) {
        super(id, name, exchangeType, firstQuotationData);
    }

    public RealStockQuotation(String id, String name, Dividends dividends, QuotationInfo firstQuotationData) {
        super(id, name, dividends, firstQuotationData);
    }

    public RealStockQuotation(String id, String name, Dividends dividends, ExchangeType exchangeType, QuotationInfo firstQuotationData) {
        super(id, name, dividends, exchangeType, firstQuotationData);
    }

    public RealStockQuotation(ConfigurationSection config) {
        super(config);
    }

    @Override
    public void refreshQuotation() {

        Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {

            try {
                double price = Stonks.plugin.stockAPI.getPrice(getId());
                int datanumber = Stonks.plugin.configManager.quotationDataNumber;
                //We update all the data List
                for (TimeScale time : TimeScale.values()) {
                    //We get the list corresponding to the time
                    List<QuotationInfo> workingData = new ArrayList<>();
                    Validate.notNull(this.getData(time), "The data for" + getId() + "for" + time.toString() + "is null");
                    workingData.addAll(this.getData(time));
                    //If the the latest data of workingData is too old we add another one
                    if (System.currentTimeMillis() - workingData.get(workingData.size() - 1).getTimeStamp() > time.getTime() / datanumber) {

                        workingData.add(new QuotationInfo(System.currentTimeMillis(), price));
                        //If the list contains too much data we remove the older ones
                        if (workingData.size() > datanumber)
                            workingData.remove(0);
                        //We save the changes we made in the attribute
                        this.setData(time, workingData);
                    }
                }
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

