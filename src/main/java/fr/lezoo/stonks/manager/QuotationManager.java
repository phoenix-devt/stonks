package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
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
    }

    public void remove(String quotationId) {
        Validate.isTrue(!mapped.containsKey(quotationId), "Tried to remove quotation " + quotationId + " which does not exist");

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
