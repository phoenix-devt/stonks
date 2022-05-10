package fr.lezoo.stonks.stock.api;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public abstract class StockAPI {
    private final String apiKey;

    public StockAPI(ConfigurationSection config) {
        this(config.getString("key"));
    }

    public StockAPI(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "Could not find API key");
    }

    /**
     * @param stockId the id/symbol of the stock we want to have the price
     * @return the current price of the stock
     */
    public double getPrice(String stockId) throws URISyntaxException, IOException, InterruptedException, ParseException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(new URI(getURL(stockId))).GET().build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(httpResponse.body(), stockId);
    }

    public String getStockKey() {
        return apiKey;
    }

    public abstract String getURL(String stockId);

    public abstract double parseResponse(String response, String stockId) throws ParseException;

    @NotNull
    public static StockAPI fromConfig(ConfigurationSection config) {
        String used = config.getString("used");
        switch (used) {
            case "finnhub":
                return new FinnhubAPI(config);
            case "alphavantage":
                return new AlphaVantageAPI(config);
            case "twelvedata":
                return new TwelveDataAPI(config);
            default:
                throw new RuntimeException("Could not match stock API to '" + used + "'");
        }
    }
}
