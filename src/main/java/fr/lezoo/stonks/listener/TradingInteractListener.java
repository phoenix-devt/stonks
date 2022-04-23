package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.OrderInfo;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.InputHandler;
import fr.lezoo.stonks.util.SimpleChatInput;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.util.Vector;

import java.util.List;

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




        for (Board board : Stonks.plugin.boardManager.getBoards()) {
            //We get the perpendicular straight line
            Location boardLocation = board.getLocation().clone();
            Vector perpendicular = Utils.rotateAroundY(board.getDirection()).getDirection();
            double scalar = (boardLocation.clone().subtract(player.getLocation()).toVector().dot(perpendicular));
            //if the scalar product is positive we are behind the block and if it is too big we are too far
            if (scalar >= 0 || scalar <= -Stonks.plugin.configManager.maxInteractionDistance)
                return;
            Location location = player.getEyeLocation();
            //We normalize the vector so that coordinate corresponding to perpendicular is one
            Vector direction = player.getEyeLocation().getDirection().multiply(1 / (player.getEyeLocation().getDirection().dot(perpendicular)));
            direction.multiply(scalar);
            //After that we have a better precision
            location.add(direction);

            //Origine de l'offset arrondi a la valeur pour le board
            //Between  and 0 and 1, represents where the board is
            verticalOffset = (boardLocation.getY() + board.getHeight() - location.getY()) / board.getHeight();
            //The same, we use a scalar product
            horizontalOffset = location.subtract(boardLocation).toVector().dot(board.getDirection().getDirection()) / board.getWidth();


            //If we are really clicking on a board we check where it has been clicked and stop the method

            if (horizontalOffset >= 0 && horizontalOffset <= 1 && verticalOffset >= 0 && verticalOffset <= 1) {
                this.board = board;
                //We then check if it corresponds to the location of a button
                checkDownSquare();
                checkMiddleDownSquare();
                checkMiddleUpSquare();
                checkUpSquare();
                return;
            }
        }

    }


    //Does what need to be done when the top square is touched
    public void checkUpSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.21) && (verticalOffset > 0.02)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            //We set the player current quotation to the actual one
            playerData.setCurrentQuotation(board.getQuotation());
            OrderInfo orderInfo = playerData.getOrderInfo(board.getQuotation().getId());

            Message.SET_PARAMETER_ASK.format("leverage", "\n" + orderInfo.getLeverage(),
                    "amount", orderInfo.hasAmount() ? "\n" + orderInfo.getAmount() : "",
                    "min-price", orderInfo.hasMinPrice() ? "\n" + orderInfo.getMinPrice() : "",
                    "max-price", orderInfo.hasMaxPrice() ? "\n" + orderInfo.getMaxPrice() : "").send(player);
            //We listen to the player
            new SimpleChatInput(playerData, InputHandler.SET_PARAMETER_HANDLER);


        }
    }

    public void checkMiddleUpSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.45) && (verticalOffset > 0.25)) {

            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getQuotation(), ShareType.NORMAL);
        }
    }


    public void checkMiddleDownSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.7) && (verticalOffset > 0.5)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getQuotation(), ShareType.SHORT);
        }
    }

    public void checkDownSquare() {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.95) && (verticalOffset > 0.75)) {
            Stonks.plugin.configManager.QUOTATION_LIST.generate(Stonks.plugin.playerManager.get(player)).open();
        }
    }
}
