package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.board.Board;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.UUID;

public class TradingInteractListener implements Listener {

    private double verticalOffset;
    private double horizontalOffset;
    private Player player;
    private Board board;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        player = event.getPlayer();

        //We check if the player is left clicking
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;


        //Check if the player is trying to interact with a board with a tradingbook
        if (player.getInventory().getItemInMainHand() != null && player.getInventory().getItemInMainHand().getItemMeta() != null
                && player.getInventory().getItemInMainHand().getItemMeta().getDisplayName().equalsIgnoreCase(Stonks.plugin.configManager.tradingBookName)) {

            // We get the block that the player is interacting with and check if
            // the nearest entity is an item frame wich belongs to a board
            Block block = player.getTargetBlockExact(Stonks.plugin.configManager.maxInteractionDistance);
            if (block == null) {

                return;
            }
            double minDistance = 2;
            ItemFrame itemFrame = null;
            for (Entity entity : block.getLocation().getChunk().getEntities()) {
                if (entity.getLocation().distance(block.getLocation()) < minDistance && entity instanceof ItemFrame) {
                    minDistance = entity.getLocation().distance(block.getLocation());
                    itemFrame = (ItemFrame) entity;

                }
            }

            //If there is no itemFrame the player is not interacting with a board
            if (itemFrame == null) {
                return;
            }

            //We get the board of the entity
            PersistentDataContainer container = itemFrame.getPersistentDataContainer();
            //If it is not a Stonks itemFrame we return nothing
            if (!container.has(new NamespacedKey(Stonks.plugin, "boarduuid"), PersistentDataType.STRING))
                return;
            board = Stonks.plugin.boardManager.getBoard(UUID.fromString(container.get(new NamespacedKey(Stonks.plugin, "boarduuid"), PersistentDataType.STRING)));

            //We get the perpendicular straight line
            Vector perpendicular = Utils.getItemFrameDirection(board.getDirection());

            double scalar = (itemFrame.getLocation().subtract(player.getLocation()).toVector().dot(perpendicular));
            //if the scalar product is positive we are behind the block
            if (scalar >= 0)
                return;
            Location location = player.getEyeLocation();
            //We normalize the vector so that coordonate corresponding to perpendicular is one
            Vector direction = player.getEyeLocation().getDirection().multiply(1 / (player.getEyeLocation().getDirection().dot(perpendicular)));
            direction.multiply(scalar);

            //After that we have a better precision
            location.add(direction);

            //Between  and 0 and 1, represents where the board is
            verticalOffset = (board.getLocation().getY() + board.getHeight() - location.getY()) / board.getHeight();
            //The same, we use a scalar product
            horizontalOffset = location.subtract(board.getLocation()).toVector().dot(board.getDirection().getDirection()) / board.getWidth();

            //We then check if it corresponds to the location of a button
            checkDownSquare();
            checkMiddleDownSquare();
            checkMiddleUpSquare();
            checkUpSquare();

        }


    }


    //Does what need to be done when the top square is touched
    public void checkUpSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.21) && (verticalOffset > 0.02)) {
            player.sendMessage("" + 0);
        }
    }

    public void checkMiddleUpSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.45) && (verticalOffset > 0.25)) {

            //We do nothing if there is a problem
            if (readBook() == null)
                return;
            // Market is closing!
            if (Stonks.plugin.isClosed()) {
                Message.MARKET_CLOSING.format().send(player);
                return;
            }
            PlayerData playerData =Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getQuotation(), ShareType.POSITIVE,readBook()[0],readBook()[1],readBook()[2]);
        }
    }


    public void checkMiddleDownSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.7) && (verticalOffset > 0.5)) {
            //We do nothing if there is a problem
            if (readBook() == null)
                return;
            // Market is closing!
            if (Stonks.plugin.isClosed()) {
                Message.MARKET_CLOSING.format().send(player);
                return;
            }
            PlayerData playerData =Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getQuotation(), ShareType.SHORT,readBook()[0],readBook()[1],readBook()[2]);
        }
    }

    public void checkDownSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.95) && (verticalOffset > 0.75)) {
            player.sendMessage("" + 3);
        }
    }


    @Nullable
    private double[] readBook() {
        ItemStack book = player.getInventory().getItemInMainHand();
        BookMeta meta = (BookMeta) book.getItemMeta();
        String[] lines = meta.getPages().get(0).split("\n");

        //We look at line4,5,6
        double amount = 0;
        if (lines.length == 3) {
            player.sendMessage(ChatColor.RED+"You didn't enter anything, write after the instructions by coming back to the line");
            return null;
        }
        //We cast into double but want it to be an int, we cant lose half a diamond... easier to do so.
        try {
            amount = (double) Integer.parseInt(lines[3]);
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "You didn't enter the money normally");
            return null;
        }
        double maxPrice = Float.POSITIVE_INFINITY;
        double minPrice = 0;
        if (lines.length >= 5) {
            try {
                maxPrice = Double.parseDouble(lines[4]);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "You didn't enter the maxPrice normally");
                return null;
            }
        }
        if (lines.length >= 6) {
            try {
                minPrice = Double.parseDouble(lines[5]);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "You didn't enter the minPrice normally");
                return null;
            }
        }
        return new double[]{amount, maxPrice, minPrice};
    }
}
