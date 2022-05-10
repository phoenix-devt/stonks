package fr.lezoo.stonks.stock.api;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TwelveDataAPI extends StockAPI {
    public TwelveDataAPI(ConfigurationSection config) {
        super(config);
    }

    @Override
    public String getURL(String stockId) {
        return "https://api.twelvedata.com/price?symbol=" + stockId + "&apikey=" + getStockKey();
    }

    @Override
    public double parseResponse(String response, String stockId) throws ParseException {
        Validate.isTrue(!response.contains("\"code\":429"), "Max amount of 800 API calls/day (for free version of twelve data api) has been surpassed");
        Validate.isTrue(!response.contains("\"code\":401"), "Wrong API key for Twelve Data");

        JSONObject object = (JSONObject) new JSONParser().parse(response);
        Validate.notNull(object.get("price"), "TwelveData API Problem with" + stockId + "\n" + response);
        return Double.parseDouble(object.get("price").toString());
    }
}
