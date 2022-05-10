package fr.lezoo.stonks.stock.api;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class FinnhubAPI extends StockAPI {
    public FinnhubAPI(ConfigurationSection config) {
        super(config);
    }

    @Override
    public String getURL(String stockId) {
        return "https://finnhub.io/api/v1/quote?symbol=" + stockId.toUpperCase() + "&token=" + getStockKey();
    }

    @Override
    public double parseResponse(String response, String stockId) throws ParseException {
        Validate.isTrue(!response.contains("API limit reached"), "Max amount of 60 API calls/min (for free version of Finnhub) has been surpassed\"" +
                " reduce the number of real stock stocks or the the stock-data-number in config.yml ");
        JSONObject object = (JSONObject) new JSONParser().parse(response);
        Validate.notNull(object.get("c"), "Finnhub API Problem with" + stockId + "\n" + response);
        return Double.parseDouble(object.get("c").toString());
    }
}
