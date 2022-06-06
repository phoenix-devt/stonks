package fr.lezoo.stonks.stock.handler;

import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.share.ShareType;
import org.bukkit.configuration.ConfigurationSection;

public interface StockHandler {

    void refreshPrice();

    void saveInFile(ConfigurationSection config);

    void whenBought(ShareType type, double shares);

    double getCurrentPrice();

    /**
     * This method fixes a dupe issue. Since buying X shares makes
     * the stock price increases, the player can just instantly resell
     * his share to "surf on his own wave" and generate infinite money.
     * <p>
     * The best fix is to have the share gain be calculated NOT based on
     * the instantaneous stock price but on the price the stock WOULD have
     * if the share had never existed.
     * <p>
     * This method requires a stock price computation. That method is useless
     * for real stocks because the investor has NO influence whatsoever on the stock.
     *
     * @param share Share being sold
     * @return Price of a share when buying it
     */
    double getSellPrice(Share share);
}
