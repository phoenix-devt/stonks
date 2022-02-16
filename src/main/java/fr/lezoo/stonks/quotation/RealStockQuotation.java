package fr.lezoo.stonks.quotation;

import fr.lezoo.stonks.Stonks;
import org.bukkit.configuration.ConfigurationSection;
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
        HttpClient client = HttpClient.newHttpClient();
        String url = "https://finnhub.io/api/v1/quote?symbol=" + getId() + "&token=c3vd9a2ad3ia9vcboc9g";
        try {
            HttpRequest request = HttpRequest.newBuilder(new URI(url)).GET().build();
            HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
            String response = httpResponse.body();
            JSONObject object = (JSONObject) new JSONParser().parse(response);
            Double price = Double.parseDouble((String) object.get("c"));

            int datanumber = Stonks.plugin.configManager.quotationDataNumber;
            //We update all the data List
            for (TimeScale time : TimeScale.values()) {
                //We get the list corresponding to the time
                List<QuotationInfo> workingData = new ArrayList<>();
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
    }
}

