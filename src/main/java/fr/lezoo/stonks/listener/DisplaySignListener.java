package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.StockPriceUpdateEvent;
import fr.lezoo.stonks.display.sign.DisplaySign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DisplaySignListener implements Listener {

    @EventHandler
    public void a(StockPriceUpdateEvent event) {
        for (DisplaySign sign : Stonks.plugin.signManager.getByStock(event.getStock()))
            sign.update();
    }
}
