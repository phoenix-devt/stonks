package fr.lezoo.stonks.util;

import fr.lezoo.stonks.gui.api.PluginInventory;
import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.player.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.BiFunction;

public class ChatInput extends TemporaryListener {
    private final PluginInventory inv;
    private final BiFunction<PlayerData, String, Boolean> inputHandler;

    /**
     * @param inv          Custom inventory opened atm
     * @param inputHandler Function evaluated every time the player inputs
     *                     something in the chat. Should return true if the chat
     *                     input mecanism should close
     */
    public ChatInput(PluginInventory inv, BiFunction<PlayerData, String, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), InventoryOpenEvent.getHandlerList());

        this.inv = inv;
        this.inputHandler = inputHandler;

        // Close current inventory
        inv.getPlayer().closeInventory();
    }

    @Override
    public void whenClosed() {

    }

    public PlayerData getPlayer() {
        return inv.getPlayerData();
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void a(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().equals(inv.getPlayerData().getPlayer()))
            return;

        event.setCancelled(true);

        if (inputHandler.apply(inv.getPlayerData(), event.getMessage())) {
            close();
            inv.open();
        }
    }

    @EventHandler
    public void b(InventoryOpenEvent event) {
        if (event.getPlayer().equals(inv.getPlayer()))
            close();
    }
}
