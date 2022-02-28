package fr.lezoo.stonks.manager.StockAPI;

import fr.lezoo.stonks.Stonks;
import org.apache.commons.lang.Validate;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TwelveDataAPI  extends StockAPIManager{


    @Override
    public String getURL(String quotationId) {
        return "https://api.twelvedata.com/price?symbol="+quotationId+"&apikey="+ Stonks.plugin.configManager.apiKey;
    }

    @Override
    public double parseResponse(String response, String quotationId) throws ParseException {
        Validate.isTrue(!response.contains("\"code\":429"), "Max amount of 800 API calls/day (for free version of twelve data api) has been surpassed");
        Validate.isTrue(!response.contains("\"code\":401"), "Wrong API key for Twelve Data");

        JSONObject object= (JSONObject) new JSONParser().parse(response);
        Validate.notNull(object.get("price"),"TwelveData API Problem with" + quotationId + "\n" + response);
        return Double.parseDouble(object.get("price").toString());
    }
}
