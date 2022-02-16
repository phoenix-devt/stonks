package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.RealStockQuotation;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

public class QuotationManager implements FileManager {
    private final Map<String, Quotation> mapped = new HashMap<>();

    public void refreshQuotations() {
        mapped.values().forEach((q) -> q.refreshQuotation());
    }

    @Override
    public void load() {

        FileConfiguration config = new ConfigFile("quotations").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Quotation(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load quotation '" + key + "': " + exception.getMessage());
            }

        //We load the real stock quotations
        loadRealStockQuotation();
    }


    public void loadRealStockQuotation() {
        HashMap<String, String> quotationSymbolsNames = getQuotationSymbolsNames();
        HttpClient client = HttpClient.newHttpClient();
        for (String quotationId : quotationSymbolsNames.keySet()) {
            if (!mapped.containsKey(quotationId)) {
                String url = "https://finnhub.io/api/v1/quote?symbol=" + quotationId + "&token=c3vd9a2ad3ia9vcboc9g";
                try {
                    HttpRequest request = HttpRequest.newBuilder(new URI(url)).GET().build();
                    HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                    String response = httpResponse.body();
                    JSONObject object = (JSONObject) new JSONParser().parse(response);
                    Double price = Double.parseDouble(object.get("c").toString());

                    Quotation quotation = new RealStockQuotation(quotationId, quotationSymbolsNames.get(quotationId)
                            , new QuotationInfo(System.currentTimeMillis(), price));
                    mapped.put(quotationId, quotation);

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }


        }
    }

    /**
     * @return The map with the id of each real stock quotation linked to its name to add them to the existing quotations list
     */
    public HashMap<String, String> getQuotationSymbolsNames() {
        HashMap<String, String> quotationSymbolsNames = new HashMap<>();
        quotationSymbolsNames.put("AAPL", "Apple");
        quotationSymbolsNames.put("MSFT", "Microsoft");
        quotationSymbolsNames.put("GOOGL", "Alphabet");
        quotationSymbolsNames.put("AMZN", "Amazon");
        quotationSymbolsNames.put("TSLA", "Tesla");
        quotationSymbolsNames.put("BRK-A", "Berkshire Hathaway");
        quotationSymbolsNames.put("NVDA", "Nvidia");
        quotationSymbolsNames.put("TSM", "Taiwan Semiconductor");
        quotationSymbolsNames.put("FB", "Meta");
        quotationSymbolsNames.put("V", "Visa");
        quotationSymbolsNames.put("JPM", "JPMorgan Chase");
        quotationSymbolsNames.put("UNH", "UnitedHealth");
        quotationSymbolsNames.put("JNJ", "Johnson & Johnson");
        quotationSymbolsNames.put("BAC", "Bank of America");
        quotationSymbolsNames.put("PG", "Nike");
        quotationSymbolsNames.put("MA", "Mastercard");
        quotationSymbolsNames.put("WMT", "Walmart");
        quotationSymbolsNames.put("HD", "Home Depot");
        quotationSymbolsNames.put("BABA", "Nvidia");
        quotationSymbolsNames.put("XOM", "ExxonMobil");
        quotationSymbolsNames.put("DIS", "Disney");
        quotationSymbolsNames.put("PFE", "Pfizer");
        quotationSymbolsNames.put("ASML", "ASML");
        quotationSymbolsNames.put("KO", "Coca-Cola");
        quotationSymbolsNames.put("TM", "Toyota");
        quotationSymbolsNames.put("CVX", "Chevron");
        quotationSymbolsNames.put("ABBV", "AbbVie");
        quotationSymbolsNames.put("AVGO", "Broadcom");
        quotationSymbolsNames.put("LLY", "Eli Lilly");
        quotationSymbolsNames.put("WFC", "Wells Fargo");
        quotationSymbolsNames.put("NKE", "Nike");
        quotationSymbolsNames.put("PEP", "Pepsi");
        quotationSymbolsNames.put("CSCO", "Cisco");
        quotationSymbolsNames.put("COST", "Costco");
        quotationSymbolsNames.put("ADBE", "Adobe");
        quotationSymbolsNames.put("VZ", "Verizon");
        quotationSymbolsNames.put("TMO", "Thermo Fisher");
        quotationSymbolsNames.put("ABT", "Abbott Labs");
        quotationSymbolsNames.put("ORCL", "Oracle");
        quotationSymbolsNames.put("CMCSA", "Comcast");
        quotationSymbolsNames.put("ACN", "Accenture");
        quotationSymbolsNames.put("CRM", "Salesforce");
        quotationSymbolsNames.put("INTC", "Intel");
        quotationSymbolsNames.put("MRK", "Merck");
        quotationSymbolsNames.put("QCOM", "Qualcomm");
        quotationSymbolsNames.put("NVS", "Novartis");
        quotationSymbolsNames.put("DHR", "Danaher");
        quotationSymbolsNames.put("MCD", "McDonalds");
        quotationSymbolsNames.put("UPS", "UPS");
        quotationSymbolsNames.put("AZN", "AstraZeneca");

        return quotationSymbolsNames;
    }


    public void remove(String quotationId) {
        Validate.isTrue(mapped.containsKey(quotationId), "Tried to remove quotation " + quotationId + " which does not exist");

        mapped.remove(quotationId);
    }

    @Override
    public void save() {
        ConfigFile config = new ConfigFile("quotations");

        // Remove old quotations
        for (String key : config.getConfig().getKeys(true))
            config.getConfig().set(key, null);

        // Save newest
        for (Quotation quotation : mapped.values())
            quotation.save(config.getConfig());

        config.save();
    }

    public boolean has(String id) {
        return mapped.containsKey(formatId(id));
    }

    /**
     * Gets the quotation with corresponding ID, or throws an IAE
     *
     * @param id Quotation identifier
     * @return Corresponding quotation
     */
    @NotNull
    public Quotation get(String id) {
        Validate.isTrue(mapped.containsKey(formatId(id)), "No quotation found with ID '" + formatId(id) + "'");
        return mapped.get(formatId(id));
    }

    public void register(Quotation quotation) {
        Validate.isTrue(!mapped.containsKey(quotation.getId()), "There is already a quotation with ID " + quotation.getId() + "'");

        mapped.put(quotation.getId(), quotation);
    }

    public Collection<Quotation> getQuotations() {
        return mapped.values();
    }

    private String formatId(String str) {
        return str.toLowerCase().replace(" ", "-").replace("_", "-");
    }
}
