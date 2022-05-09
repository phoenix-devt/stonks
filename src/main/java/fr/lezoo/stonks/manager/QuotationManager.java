package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.TimeScale;
import fr.lezoo.stonks.quotation.handler.RealStockHandler;
import fr.lezoo.stonks.util.ConfigFile;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class QuotationManager implements FileManager {
    private final Map<String, LoadedQuotation> mapped = new HashMap<>();

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

    public void remove(String quotationId) {
        Validate.isTrue(mapped.containsKey(quotationId), "Tried to remove quotation " + quotationId + " which does not exist");

        LoadedQuotation removed = mapped.remove(quotationId);
        removed.refreshRunnable.cancel();
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
        for (LoadedQuotation loaded : mapped.values())
            loaded.quotation.save(config.getConfig());

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
        return mapped.get(formatId(id)).quotation;
    }

    public void register(Quotation quotation) {
        Validate.isTrue(!mapped.containsKey(quotation.getId()), "There is already a quotation with ID " + quotation.getId() + "'");
        mapped.put(quotation.getId(), new LoadedQuotation(quotation));
    }

    public void forEachQuotation(Consumer<Quotation> action) {
        for (LoadedQuotation loaded : mapped.values())
            action.accept(loaded.quotation);
    }

    public Set<Quotation> getQuotations() {
        Set<Quotation> set = new HashSet<>();
        for (LoadedQuotation loaded : mapped.values())
            set.add(loaded.quotation);
        return set;
    }

    private String formatId(String str) {
        return str.toLowerCase().replace(" ", "-").replace("_", "-");
    }

    class LoadedQuotation {
        final Quotation quotation;
        final BukkitRunnable refreshRunnable;

        LoadedQuotation(Quotation quotation) {
            this.quotation = quotation;
            this.refreshRunnable = new BukkitRunnable() {
                @Override
                public void run() {
                    quotation.getHandler().refresh();
                }
            };
            refreshRunnable.runTaskTimer(Stonks.plugin, 20, 20 * quotation.getRefreshPeriod());
        }
    }

    /**
     * set the quotation data for a quotation when QuotationManager loads
     *
     * @param quotation The quotation which data needs to be set
     */
    public void initializeQuotationData(Quotation quotation) {
        // We load the different data from the yml if they exist
        if (new ConfigFile("quotations-data").getConfig().getKeys(false).contains(quotation.getId())) {

            ConfigurationSection section = new ConfigFile("quotations-data").getConfig().getConfigurationSection(quotation.getId());


            for (TimeScale time : TimeScale.values()) {
                int i = 0;
                List<QuotationInfo> workingQuotation = new ArrayList<>();

                while (section.contains(time.toString().toLowerCase() + "data." + i)) {
                    workingQuotation.add(new QuotationInfo(section.getConfigurationSection(time.toString().toLowerCase() + "data." + i)));
                    i++;
                }

                // We change the attribute
                quotation.setData(time, workingQuotation);

            }
        }
        //Otherwise we create the firstQuotationData depending on the quotation type
        else {


            if (quotation.getHandler() instanceof RealStockHandler) {

                Bukkit.getScheduler().runTaskAsynchronously(Stonks.plugin, () -> {
                    try {
                        double price = Stonks.plugin.stockAPI.getPrice(quotation.getId());
                        QuotationInfo firstQuotationData = new QuotationInfo(System.currentTimeMillis(), price);
                        for (TimeScale disp : TimeScale.values())
                            quotation.setData(disp, Arrays.asList(firstQuotationData));

                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                });

            }
            //If it a virtual Quotation we set the initial price at 10 if there is no data for the quotation
            else {
                for (TimeScale disp : TimeScale.values())
                    if (quotation.getData(disp).size() == 0)
                        quotation.setData(disp, Arrays.asList(new QuotationInfo(System.currentTimeMillis(), 10)));

            }
        }

    }


    /**
     * Saves all the data of the quotations in quotation-data.yml file
     */
    public void save(Quotation quotation) {
        ConfigFile configFile = new ConfigFile("quotations-data");
        FileConfiguration config = configFile.getConfig();
        //We remove the old data
        if (config.contains(quotation.getId())) {
            ConfigurationSection section = config.getConfigurationSection(quotation.getId());
            for (String key : section.getKeys(true)) {
                section.set(key, null);

            }
        }
        //We save the information of the data using quotationDataManager
        for (TimeScale time : TimeScale.values()) {
            List<QuotationInfo> quotationData = quotation.getData(time);
            //We load the data needed
            for (int i = 0; i < quotationData.size(); i++) {
                config.set(quotation.getId() + "." + time.toString().toLowerCase() + "data." + i + ".price", quotationData.get(i).getPrice());
                config.set(quotation.getId() + "." + time.toString().toLowerCase() + "data." + i + ".timestamp", quotationData.get(i).getTimeStamp());
            }
        }
        configFile.save();
    }
}
