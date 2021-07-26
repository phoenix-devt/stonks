package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.NBTItem;
import fr.lezoo.stonks.api.quotation.Board;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class RemoveBoardListener implements Listener {
    Player player;

    public RemoveBoardListener(Player player) {
        this.player = player;
    }

    @EventHandler
    public void onBreak(HangingBreakByEntityEvent e) {
        //If the block wasn't removed by the person we are listenning to we dont do anything
        if (!(e.getRemover() instanceof Player))
            return;
        if (((Player) e.getRemover() != player))
            return;

        if (!(e.getEntity() instanceof ItemFrame))
            return;

        ItemFrame itemFrame = (ItemFrame) e.getEntity();
        NBTItem nbtItem = NBTItem.get((ItemStack) itemFrame);

        //We check if it a Stonks board and get the uuid of the board
        if (nbtItem.hasTag("boarduuid")) {
            Board board = Stonks.plugin.boardManager.getBoard(UUID.fromString(nbtItem.getString("boarduuid")));
            board.destroy();
            //We unregister the listener
            HandlerList.unregisterAll(this);
        }


    }
}
