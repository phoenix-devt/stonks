package fr.lezoo.stonks.listener.temp;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.sign.DisplaySign;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.util.Position;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SignDisplayEditionListener extends TemporaryListener {
    private final Quotation quotation;
    private final Player player;
    private final boolean removing;

    /**
     * @param player   Player editing a display sign
     * @param removing True: sign being removing; false: sign being created
     */
    public SignDisplayEditionListener(Quotation quotation, Player player, boolean removing) {
        super(PlayerQuitEvent.getHandlerList(), PlayerInteractEvent.getHandlerList());

        this.quotation = quotation;
        this.player = player;
        this.removing = removing;
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

        // Sign removal
        if (removing) {
            if (!Stonks.plugin.signManager.has(pos)) {
                player.sendMessage(ChatColor.RED + "No display sign was found at that location.");
                return;
            }

            Stonks.plugin.signManager.unregister(pos);
            close();
            player.sendMessage(ChatColor.YELLOW + "You successfully registered a display sign at the target location for " + quotation.getName());
            return;
        }

        // Sign creation
        if (Stonks.plugin.signManager.has(pos)) {
            player.sendMessage(ChatColor.RED + "A display sign is always registered at that location.");
            return;
        }

        DisplaySign sign = new DisplaySign(quotation, pos);
        Stonks.plugin.signManager.register(sign);
        player.sendMessage(ChatColor.YELLOW + "You successfully registered a display sign at the target location for " + quotation.getName());
        close();
    }

    @Override
    public void whenClosed() {

    }
}
