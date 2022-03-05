package fr.lezoo.stonks.quotation.api;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class AlphaVantageAPI extends StockAPI {
    public AlphaVantageAPI(ConfigurationSection config) {
        super(config);
    }

    @Override
    public String getURL(String quotationId) {
        return "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + quotationId + "&apikey=" + getStockKey();
    }

    @Override
    public double parseResponse(String response, String quotationId) throws ParseException {
        //Check if the amount of API call has been surpassed
        Validate.isTrue(!response.contains("Our standard API call frequency is 5 calls"), "Max amount of 5 API calls/min (for free version of alphavantage) has been surpassed");
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(response);
        JSONObject globalQuotes = (JSONObject) jsonObject.get("Global Quote");
        Validate.notNull(globalQuotes.get("05. price"), "AlphaVantage API Problem with" + quotationId + "\n" + response);
        return Double.parseDouble(globalQuotes.get("05. price").toString());
    }
}
