package fr.lezoo.stonks.util;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.StockInventory;
import fr.lezoo.stonks.gui.objects.PluginInventory;
import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatInput extends TemporaryListener {
    private final PluginInventory inv;
    private final TriFunction<PlayerData, String, Stock, Boolean> inputHandler;

    /**
     * @param inv          Custom inventory opened atm
     * @param inputHandler Function evaluated every time the player inputs
     *                     something in the chat. Should return true if the chat
     *                     input mecanism should close
     */
    public ChatInput(PluginInventory inv, TriFunction<PlayerData, String, Stock, Boolean> inputHandler) {
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
        Bukkit.getScheduler().runTask(Stonks.plugin, () -> {
            if (inputHandler.apply(inv.getPlayerData(), event.getMessage(), inv instanceof StockInventory ? ((StockInventory) inv).getStock() : null)) {
                close();
                inv.open();
            }
        });
    }

    @EventHandler
    public void b(InventoryOpenEvent event) {
        if (event.getPlayer().equals(inv.getPlayer()))
            close();
    }
}
