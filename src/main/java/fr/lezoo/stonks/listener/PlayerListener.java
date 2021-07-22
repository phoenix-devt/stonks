package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void a(PlayerJoinEvent event) {
        Stonks.plugin.playerManager.setup(event.getPlayer());
    }
}
