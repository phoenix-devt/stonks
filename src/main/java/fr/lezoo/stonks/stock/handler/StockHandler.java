package fr.lezoo.stonks.stock.handler;

import org.bukkit.configuration.ConfigurationSection;

public interface StockHandler {
    /**
     * Adds the value of the price for each time scale
     */
    void refresh();

    void refreshPrice();

    void saveInFile(ConfigurationSection config);

    void whenBought(double signedShares);

    double getCurrentPrice();

    /**
     * This method fixes a dupe issue. Since buying X shares makes
     * the stock price increases, the player can just instantly resell
     * his share to "surf on his own wave" and generate infinite money.
     * <p>
     * A fix is to consider that the share was bought at the price
     * the stock has AFTER the share is bought. This method calculates
     * the price of the stock if the stock were to have an extra
     * amount of shares in the server.
     *
     * @param signedShares Amount of shares bought, negative for shorting.
     * @return Price of a share when buying it
     */
    double getShareInitialPrice(double signedShares);
}
