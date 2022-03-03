package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.RealStockQuotation;
import fr.lezoo.stonks.quotation.TimeScale;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;


/**
 * This manager allows to load and save the data of each quotation in a separate yml
 */
public class QuotationDataManager {

//Doesn't need to be loaded since it reads directly into the yml

    public QuotationDataManager() {

    }


    /**
     * set the quotation data for a quotation when QuotationManager loads
     *
     * @param quotation The quotation which data needs to be set
     */
    public void setQuotationData(Quotation quotation) {
        // We load the different data from the yml if they exist
        if (new ConfigFile("quotations-data").getConfig().getKeys(false).contains(quotation.getId())) {

            ConfigurationSection section = new ConfigFile("quotations-data").getConfig().getConfigurationSection(quotation.getId());


            for (TimeScale time : TimeScale.values()) {
                int i = 0;
                List<QuotationInfo> workingQuotation = new ArrayList<>();

                while (section.contains(time.toString().toLowerCase() + "data." + i)) {
                    workingQuotation.add(new QuotationInfo(section.getConfigurationSection(time.toString().toLowerCase() + "data." + i)));
                    i++;
                }

                // We change the attribute
                quotation.setData(time, workingQuotation);

            }
        }
        //Otherwise we create the firstQuotationData depending on the quotation type
        else {


            if (quotation instanceof RealStockQuotation) {

                HttpClient client = HttpClient.newHttpClient();

                Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {
                    try {
                        double price = Stonks.plugin.stockAPIManager.getPrice(quotation.getId());
                        QuotationInfo firstQuotationData = new QuotationInfo(System.currentTimeMillis(), price);
                        for (TimeScale disp : TimeScale.values())
                            quotation.setData(disp, Arrays.asList(firstQuotationData));

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
            //If it a virtual Quotation we set the initial price at 10
            else {
                for (TimeScale disp : TimeScale.values())
                    quotation.setData(disp, Arrays.asList(new QuotationInfo(System.currentTimeMillis(), 10)));

            }
        }

    }


    /**
     * Saves all the data of the quotations in quotation-data.yml file
     */
    public void save(Quotation quotation) {
        ConfigFile configFile = new ConfigFile("quotations-data");
        FileConfiguration config = configFile.getConfig();

        //We remove the old data
        ConfigurationSection section = config.getConfigurationSection(quotation.getId());
        for (String key : section.getKeys(true)) {
            section.set(key, null);
        }
        //We save the information of the data using quotationDataManager
        for (TimeScale time : TimeScale.values()) {
            List<QuotationInfo> quotationData = quotation.getData(time);
            //We load the data needed
            for (int i = 0; i < quotationData.size(); i++) {
                config.set(quotation.getId() + "." + time.toString().toLowerCase() + "data." + i + ".price", quotationData.get(i).getPrice());
                config.set(quotation.getId() + "." + time.toString().toLowerCase() + "data." + i + ".timestamp", quotationData.get(i).getTimeStamp());
            }
        }
        configFile.save();
    }
}
