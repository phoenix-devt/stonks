package fr.lezoo.stonks.manager.StockAPI;

import fr.lezoo.stonks.Stonks;
import org.apache.commons.lang.Validate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FinnhubAPI extends StockAPIManager {


    @Override
    public String getURL(String quotationId) {
        return "https://finnhub.io/api/v1/quote?symbol=" + quotationId.toUpperCase() + "&token=" + Stonks.plugin.configManager.apiKey;
    }

    @Override
    public double parseResponse(String response, String quotationId) throws ParseException {
        Validate.isTrue(!response.contains("API limit reached"), "Max amount of 60 API calls/min (for free version of Finnhub) has been surpassed\"" +
                " reduce the number of real stock quotations or the the quotation-data-number in config.yml ");
        JSONObject object = (JSONObject) new JSONParser().parse(response);
        Validate.notNull(object.get("c"), "Finnhub API Problem with" + quotationId + "\n" + response);
        return Double.parseDouble(object.get("c").toString());
    }
}
