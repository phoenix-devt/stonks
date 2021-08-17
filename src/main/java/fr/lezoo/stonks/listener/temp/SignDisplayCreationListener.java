package fr.lezoo.stonks.listener.temp;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.util.Position;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.display.sign.DisplaySign;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class    SignDisplayCreationListener implements Listener {
    private final Quotation quotation;
    private final Player player;

    public SignDisplayCreationListener(Quotation quotation, Player player) {
        this.quotation = quotation;
        this.player = player;

        Bukkit.getPluginManager().registerEvents(this, Stonks.plugin);
    }

    public void close() {
        PlayerQuitEvent.getHandlerList().unregister(this);
        PlayerInteractEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void a(PlayerQuitEvent event) {
        close();
    }

    @EventHandler
    public void a(PlayerInteractEvent event) {
        if (!event.getPlayer().equals(player) || !event.hasBlock())
            return;

        Block block = event.getClickedBlock();
        if (!block.getType().name().contains("SIGN"))
            return;

        Position pos = Position.from(block.getLocation());
        if (Stonks.plugin.signManager.has(pos)) {
            player.sendMessage("A display sign is always registered at that location.");
            return;
        }

        DisplaySign sign = new DisplaySign(quotation, pos);
        Stonks.plugin.signManager.register(sign);
        player.sendMessage("");
    }
}
