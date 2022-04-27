package fr.lezoo.stonks.util;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.BiFunction;

/**
 * Listens to chat input without any inventory
 */
public class SimpleChatInput extends TemporaryListener {
    private final PlayerData playerData;
    private final BiFunction<PlayerData, String, Boolean> inputHandler;
    //The runnable will eb called when the SimpleChatInput is Closed
    private Runnable runnable = () -> {
    };


    public SimpleChatInput(PlayerData playerData, BiFunction<PlayerData, String, Boolean> inputHandler) {
        super(AsyncPlayerChatEvent.getHandlerList());
        this.playerData = playerData;
        this.inputHandler = inputHandler;
    }

    public SimpleChatInput(PlayerData playerData, BiFunction<PlayerData, String, Boolean> inputHandler, Runnable runnable) {
        super(AsyncPlayerChatEvent.getHandlerList());
        this.playerData = playerData;
        this.inputHandler = inputHandler;
        this.runnable = runnable;
    }

    /**
     * This method retruns null if the player is already ona chatInput
     */
    public static SimpleChatInput getChatInput(PlayerData playerData, BiFunction<PlayerData, String, Boolean> inputHandler) {
        if (playerData.isOnChatInput())
            return null;
        //We set the playerData to on chat input
        playerData.setOnChatInput(true);
        return new SimpleChatInput(playerData, inputHandler);
    }

    public static SimpleChatInput getChatInput(PlayerData playerData, BiFunction<PlayerData, String, Boolean> inputHandler, Runnable runnable) {
        if (playerData.isOnChatInput())
            return null;
        return new SimpleChatInput(playerData, inputHandler, runnable);
    }

    /**
     * ta
     * We close only if inputHandler accepts the message.
     */
    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!e.getPlayer().equals(playerData.getPlayer()))
            return;
        e.setCancelled(true);
        //Make it Sync
        Bukkit.getScheduler().scheduleSyncDelayedTask(Stonks.plugin, () -> {
            if (inputHandler.apply(playerData, e.getMessage()))
                close();
        });

    }


    @Override
    public void whenClosed() {
        playerData.setOnChatInput(false);
        runnable.run();
    }

}
