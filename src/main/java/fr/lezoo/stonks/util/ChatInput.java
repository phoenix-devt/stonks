package fr.lezoo.stonks.util;

import fr.lezoo.stonks.gui.QuotationInventory;
import fr.lezoo.stonks.gui.objects.PluginInventory;
import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInput extends TemporaryListener {
    private final PluginInventory inv;
    private final TriFunction<PlayerData, String, Quotation, Boolean> inputHandler;

    /**
     * @param inv          Custom inventory opened atm
     * @param inputHandler Function evaluated every time the player inputs
     *                     something in the chat. Should return true if the chat
     *                     input mecanism should close
     */
    public ChatInput(PluginInventory inv, TriFunction<PlayerData, String, Quotation, Boolean> inputHandler) {
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

        if (inputHandler.apply(inv.getPlayerData(), event.getMessage(), inv instanceof QuotationInventory ? ((QuotationInventory) inv).getQuotation() : null)) {
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
