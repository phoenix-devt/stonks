package fr.lezoo.stonks.util;

import fr.lezoo.stonks.listener.TradingInteractListener;
import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens to chat input without any inventory
 */
public class SimpleChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final Stock stock;
    private final TriFunction<PlayerData, String, Stock, Boolean> inputHandler;

    public SimpleChatInput(PlayerData playerData, Stock stock, TriFunction<PlayerData, String, Stock, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), PlayerMoveEvent.getHandlerList());

        this.playerData = playerData;
        this.stock = stock;
        this.inputHandler = inputHandler;

        playerData.setOnChatInput(true);
    }

    /**
     * Close only if the input handler accepts the message.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getPlayer().equals(playerData.getPlayer())) {
            event.setCancelled(true);
            if (inputHandler.apply(playerData, event.getMessage(), stock))
                close();
        }
    }

    @EventHandler
    public void cancel(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ())
            close();
    }

    @Override
    public void whenClosed() {
        playerData.setOnChatInput(false);
        TradingInteractListener.displayChoices(playerData, stock);
    }
}
