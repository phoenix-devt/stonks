package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.api.quotation.CreatedQuotation;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

public class QuotationManager implements FileManager {
    private final Map<String, CreatedQuotation> createdQuotationMap = new HashMap<>();

    public void refreshQuotations() {
        createdQuotationMap.values().forEach((q) -> q.refreshQuotation());
    }

    @Override
    public void load() {

        FileConfiguration config = new ConfigFile("quotations").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new CreatedQuotation(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load quotation '" + key + "': " + exception.getMessage());
            }
    }

    public void remove(String quotationId) {
        Validate.isTrue(createdQuotationMap.containsKey(quotationId),"Tried to remove the quotation"+quotationId+ "which doesn't exist!");
        createdQuotationMap.remove(quotationId);
    }


    @Override
    public void save() {
        FileConfiguration fileConfiguration = new ConfigFile("quotations").getConfig();
        //We save the quotation info
        //We remove the old ones of the yml
        for (String key : fileConfiguration.getKeys(true))
            fileConfiguration.set(key, null);
        //We save the information
        for (CreatedQuotation quotation : createdQuotationMap.values())
            quotation.save(fileConfiguration);
        try {
            fileConfiguration.save(new File(Stonks.plugin.getDataFolder(), "quotations.yml"));
        } catch (IOException e) {
            Bukkit.getServer().getLogger().log(Level.WARNING, "The quotations couldn't be saved in the yml, the file to save it doesn't exist");
            e.printStackTrace();
        }
    }

    public boolean has(String id) {
        return createdQuotationMap.containsKey(formatId(id));
    }

    /**
     * Gets the quotation with corresponding ID, or throws an IAE
     *
     * @param id Quotation identifier
     * @return Corresponding quotation
     */
    @NotNull
    public Quotation get(String id) {
        Validate.isTrue(createdQuotationMap.containsKey(formatId(id)), "No quotation found with ID '" + formatId(id) + "'");
        return createdQuotationMap.get(formatId(id));
    }

    public void register(CreatedQuotation createdQuotation) {
        Validate.isTrue(!createdQuotationMap.containsKey(createdQuotation.getId()), "There is already a quotation with ID " + createdQuotation.getId() + "'");

        createdQuotationMap.put(createdQuotation.getId(), createdQuotation);
    }

    public Collection<CreatedQuotation> getQuotations() {
        return createdQuotationMap.values();
    }

    private String formatId(String str) {
        return str.toLowerCase().replace(" ", "-").replace("_", "-");
    }
}
