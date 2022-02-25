package fr.lezoo.stonks.manager.StockAPI;

import fr.lezoo.stonks.Stonks;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public abstract class StockAPIManager {

    public static StockAPIManager getManager() {
        StockAPIManager APIManager = null;
        //load the good
        switch (Stonks.plugin.configManager.stockAPI) {
            case "finnhub": {
                APIManager = new FinnhubAPI();
                break;
            }
            case "alphavantage": {
                APIManager = new AlphaVantageAPI();
                break;
            }

        }
        //Check if the stock-api field is correctly filled
        Validate.notNull(APIManager, "The stock-api field in config.yml doesn't correspond to one of the supported stock API");
        //We verify if there is a api-key is declared
        Validate.notNull(Stonks.plugin.configManager.apiKey, "You didn't enter your api key on the config.yml file");

        return APIManager;
    }

    /**
     * @param quotationId the id/symbol of the quotation we want to have the price
     * @return the current price of the quotation
     */
    public double getPrice(String quotationId) throws URISyntaxException, IOException, InterruptedException, ParseException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(new URI(getURL(quotationId))).GET().build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        String response = httpResponse.body();
        Validate.notNull(Stonks.plugin.configManager.apiKey, "You didn't enter any apiKey in the config.yml file");

        return parseResponse(response, quotationId);

    }


    public abstract String getURL(String quotationId);

    public abstract double parseResponse(String response, String quotationId) throws ParseException;


}
