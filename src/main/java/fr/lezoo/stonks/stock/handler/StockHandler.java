package fr.lezoo.stonks.stock.handler;

import org.bukkit.configuration.ConfigurationSection;

public interface StockHandler {
    /**
     * Adds the value of the price for each time scale
     */
    void refresh();

    void refreshPrice();

    void saveInFile(ConfigurationSection config);

    void whenBought(double stocksBought);
}
