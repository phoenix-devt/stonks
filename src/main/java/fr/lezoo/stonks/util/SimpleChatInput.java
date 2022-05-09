package fr.lezoo.stonks.util;

import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Listens to chat input without any inventory
 */
public class SimpleChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final Quotation quotation;
    private final TriFunction<PlayerData, String, Quotation, Boolean> inputHandler;

    public SimpleChatInput(PlayerData playerData, Quotation quotation, TriFunction<PlayerData, String, Quotation, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList(), PlayerMoveEvent.getHandlerList());

        this.playerData = playerData;
        this.quotation = quotation;
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
            if (inputHandler.apply(playerData, event.getMessage(), quotation))
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
    }
}
