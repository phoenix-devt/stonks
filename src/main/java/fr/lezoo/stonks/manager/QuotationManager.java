package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class QuotationManager implements FileManager {
    private final Map<String, Quotation> map = new HashMap<>();

    @Override
    public void load() {

        // Register test quotation
        List<QuotationInfo> quot = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            quot.add(new QuotationInfo(System.currentTimeMillis() + 10000 * i, Math.log(1 + i)));
        Quotation quotationTest = new Quotation("aaa", "ooo", "hiiii", null, quot);
        register(quotationTest);

        FileConfiguration config = new ConfigFile("quotations").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Quotation(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load quotation '" + key + "': " + exception.getMessage());
            }
    }

    @Override
    public void save() {
        // Nothing to do here
    }

    public boolean has(String id) {
        return map.containsKey(formatId(id));
    }

    /**
     * Gets the quotation with corresponding ID, or throws an IAE
     *
     * @param id Quotation identifier
     * @return Corresponding quotation
     */
    @NotNull
    public Quotation get(String id) {
        Validate.isTrue(map.containsKey(formatId(id)), "No quotation found with ID '" + formatId(id) + "'");
        return map.get(formatId(id));
    }

    public void register(Quotation quotation) {
        Validate.isTrue(!map.containsKey(quotation.getId()), "There is already a quotation with ID '" + quotation.getId() + "'");

        map.put(quotation.getId(), quotation);
    }

    public Collection<Quotation> getQuotations() {
        return map.values();
    }

    private String formatId(String str) {
        return str.toLowerCase().replace(" ", "-").replace("_", "-");
    }
}
