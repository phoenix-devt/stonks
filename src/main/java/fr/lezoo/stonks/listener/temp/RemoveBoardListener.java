package fr.lezoo.stonks.listener.temp;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.NBTItem;
import fr.lezoo.stonks.api.quotation.board.Board;
import org.bukkit.Bukkit;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class RemoveBoardListener implements TemporaryListener {
    private final Player player;

    public RemoveBoardListener(Player player) {
        this.player = player;

        Bukkit.getPluginManager().registerEvents(this, Stonks.plugin);
    }

    @Override
    public void close() {
        HangingBreakByEntityEvent.getHandlerList().unregister(this);
    }

    @EventHandler
    public void onBreak(HangingBreakByEntityEvent event) {

        // If the block wasn't removed by the person we are listenning to we dont do anything
        if (event.getRemover().equals(player))
            return;

        if (!(event.getEntity() instanceof ItemFrame))
            return;

        ItemFrame itemFrame = (ItemFrame) event.getEntity();
        NBTItem nbtItem = NBTItem.get((ItemStack) itemFrame);

        //We check if it a Stonks board and get the uuid of the board
        if (nbtItem.hasTag("boarduuid")) {
            Board board = Stonks.plugin.boardManager.getBoard(UUID.fromString(nbtItem.getString("boarduuid")));
            board.destroy();

            //We unregister the listener
            close();
        }
    }
}
