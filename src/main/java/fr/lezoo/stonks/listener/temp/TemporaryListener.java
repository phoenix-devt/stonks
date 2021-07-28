package fr.lezoo.stonks.listener.temp;

import org.bukkit.event.Listener;

public interface TemporaryListener extends Listener {

    /**
     * Called when the temporary listener should be closed
     */
    public void close();
}
