package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.QuotationUpdateEvent;
import fr.lezoo.stonks.api.quotation.display.sign.DisplaySign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DisplaySignListener implements Listener {

    @EventHandler
    public void a(QuotationUpdateEvent event) {
        for (DisplaySign sign : Stonks.plugin.signManager.getByQuotation(event.getQuotation()))
            sign.update();
    }
}
