package fr.lezoo.stonks.quotation.handler;

import org.bukkit.configuration.ConfigurationSection;

public interface StockHandler {

    void refresh();

    void saveInFile(ConfigurationSection config);

    void whenBought(double stocksBought);
}
