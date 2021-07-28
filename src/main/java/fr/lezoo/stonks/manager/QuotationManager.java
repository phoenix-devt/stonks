package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.api.quotation.VirtualQuotation;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;

public class QuotationManager implements FileManager {
    private final Map<String, VirtualQuotation> virtualMap = new HashMap<>();

    public void refreshQuotations() {
        virtualMap.values().forEach((q)->q.refreshQuotation());
    }

    @Override
    public void load() {

        // Register test quotation
        List<QuotationInfo> quot = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            quot.add(new QuotationInfo(System.currentTimeMillis() + 10000 * i, Math.log(1 + i)));
        VirtualQuotation quotationTest = new VirtualQuotation("aaa", "ooo", "hiiii", null, quot);
        register(quotationTest);

        FileConfiguration config = new ConfigFile("quotations").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new VirtualQuotation(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load quotation '" + key + "': " + exception.getMessage());
            }
    }

    @Override
    public void save() {
        // Nothing to do here
    }

    public boolean has(String id) {
        return virtualMap.containsKey(formatId(id));
    }

    /**
     * Gets the quotation with corresponding ID, or throws an IAE
     *
     * @param id Quotation identifier
     * @return Corresponding quotation
     */
    @NotNull
    public Quotation get(String id) {
        Validate.isTrue(virtualMap.containsKey(formatId(id)), "No quotation found with ID '" + formatId(id) + "'");
        return virtualMap.get(formatId(id));
    }

    public void register(VirtualQuotation virtualQuotation) {
        Validate.isTrue(!virtualMap.containsKey(virtualQuotation.getId()), "There is already a quotation with ID '" + virtualQuotation.getId() + "'");

        virtualMap.put(virtualQuotation.getId(), virtualQuotation);
    }

    public Collection<VirtualQuotation> getQuotations() {
        return virtualMap.values();
    }

    private String formatId(String str) {
        return str.toLowerCase().replace(" ", "-").replace("_", "-");
    }
}
