package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.event.StockPriceUpdateEvent;
import fr.lezoo.stonks.display.sign.DisplaySign;
import fr.lezoo.stonks.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisplaySignListener implements Listener {

    @EventHandler
    public void updateSigns(StockPriceUpdateEvent event) {
        for (DisplaySign sign : Stonks.plugin.signManager.getByStock(event.getStock()))
            sign.update();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void clickOnSign(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType().name().endsWith("SIGN")) {
            DisplaySign sign = Stonks.plugin.signManager.getByPosition(event.getClickedBlock().getLocation());
            if (sign != null)
                Stonks.plugin.configManager.SHARE_MENU.generate(PlayerData.get(event.getPlayer()), sign.getStock()).open();
        }
    }
}
