package fr.lezoo.stonks.listener.temp;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;

public class DropItemListener extends TemporaryListener implements Listener {

    public DropItemListener() {
        super(EntityDropItemEvent.getHandlerList());
    }

    @EventHandler
    public void onItemDrop(EntityDropItemEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void whenClosed() {

    }
}
