package fr.lezoo.stonks.quotation.api;

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
        this.apiKey = Objects.requireNonNull(config.getString("key"), "Could not find API key");
    }

    public StockAPI(String apiKey) {
        this.apiKey = Objects.requireNonNull(apiKey, "Could not find API key");
    }

    /**
     * @param quotationId the id/symbol of the quotation we want to have the price
     * @return the current price of the quotation
     */
    public double getPrice(String quotationId) throws URISyntaxException, IOException, InterruptedException, ParseException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(new URI(getURL(quotationId))).GET().build();
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        return parseResponse(httpResponse.body(), quotationId);
    }

    public String getStockKey() {
        return apiKey;
    }

    public abstract String getURL(String quotationId);

    public abstract double parseResponse(String response, String quotationId) throws ParseException;

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
