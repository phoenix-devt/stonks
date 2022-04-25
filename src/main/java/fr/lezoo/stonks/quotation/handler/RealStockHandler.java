package fr.lezoo.stonks.quotation.handler;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.TimeScale;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class RealStockHandler implements StockHandler {
    private final Quotation quotation;

    public RealStockHandler(Quotation quotation) {
        this.quotation = quotation;
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
        Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {

            try {
                double price = Stonks.plugin.stockAPI.getPrice(quotation.getId());
                //We update all the data List
                for (TimeScale time : TimeScale.values()) {
                    //We get the list corresponding to the time
                    List<QuotationInfo> workingData = new ArrayList<>();
                    Validate.notNull(quotation.getData(time), "The data for " + quotation.getId() + " for " + time.toString() + " is null");
                    workingData.addAll(quotation.getData(time));
                    //If the the latest data of workingData is too old we add another one
                    if (System.currentTimeMillis() - workingData.get(workingData.size() - 1).getTimeStamp() > time.getTime() / Quotation.BOARD_DATA_NUMBER) {

                        workingData.add(new QuotationInfo(System.currentTimeMillis(), price));
                        //If the list contains too much data we remove the older ones
                        if (workingData.size() > Quotation.BOARD_DATA_NUMBER)
                            workingData.remove(0);
                        //We save the changes we made in the attribute
                        quotation.setData(time, workingData);
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
