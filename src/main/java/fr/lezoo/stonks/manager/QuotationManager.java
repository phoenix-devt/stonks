package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class QuotationManager implements FileManager {
    private final Map<String, Quotation> mapped = new HashMap<>();

    public void refreshQuotations() {
        mapped.values().forEach((q) -> q.getHandler().refresh());
    }

    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("quotations").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Quotation(config.getConfigurationSection(key)));
            } catch (RuntimeException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load quotation '" + key + "': " + exception.getMessage());
            }
    }

    public void reload() {

        // We save the quotation data if the quotation is in the quotations.yml
        ConfigFile config = new ConfigFile("quotations-data");
        for (Quotation quotation : mapped.values()) {
            if (config.getConfig().contains(quotation.getId()))
                Stonks.plugin.quotationDataManager.save(quotation);
        }

        // We remove all the quotations
        mapped.clear();

        // We load them back from the quotations.yml file
        load();
    }

    public void removeQuotation(String quotationId) {
        ConfigurationSection quotationsSection = new ConfigFile("quotations").getConfig().getConfigurationSection(quotationId);
        ConfigurationSection quotationsDataSection = new ConfigFile("quotations-data").getConfig().getConfigurationSection(quotationId);

        for (String key : quotationsSection.getKeys(true)) {
            quotationsSection.set(key, null);
        }

        for (String key : quotationsDataSection.getKeys(true)) {
            quotationsDataSection.set(key, null);
        }
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
        //Remove the data of the quotations in quotations-data.yml
        ConfigFile quotationDataConfig = new ConfigFile("quotations-data");
        for (String key : quotationDataConfig.getConfig().getKeys(true))
            quotationDataConfig.getConfig().set(key, null);

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
