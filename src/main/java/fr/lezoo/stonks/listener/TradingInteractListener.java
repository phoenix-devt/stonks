package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.display.board.BoardRaycast;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.OrderInfo;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.InputHandler;
import fr.lezoo.stonks.util.SimpleChatInput;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class TradingInteractListener implements Listener {
    private HashSet<Player> hasChatInput = new HashSet<>();


    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        //We check if the player is left clicking
        if (event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        BoardRaycast cast = new BoardRaycast(player);
        if (!cast.hasHit())
            return;

        checkDownSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
        checkMiddleDownSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
        checkMiddleUpSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
        checkUpSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
    }


    //Does what need to be done when the top square is touched
    public void checkUpSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.21) && (verticalOffset > 0.02)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            //We set the player current quotation to the actual one
            playerData.setCurrentQuotation(board.getQuotation());
            OrderInfo orderInfo = playerData.getOrderInfo(board.getQuotation().getId());

            Message.SET_PARAMETER_ASK.format("leverage", "\n" + orderInfo.getLeverage(),
                    "amount", orderInfo.hasAmount() ? "\n" + orderInfo.getAmount() : "\n",
                    "min-price", orderInfo.hasMinPrice() ? "\n" + orderInfo.getMinPrice() : "\n",
                    "max-price", orderInfo.hasMaxPrice() ? "\n" + orderInfo.getMaxPrice() : "\n").send(player);
            if (!hasChatInput.contains(player)) {

                //We listen to the player
                new SimpleChatInput(playerData, InputHandler.SET_PARAMETER_HANDLER);
                hasChatInput.add(player);
            }

        }
    }

    public void checkMiddleUpSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.45) && (verticalOffset > 0.25)) {

            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getQuotation(), ShareType.NORMAL);
        }
    }


    public void checkMiddleDownSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.7) && (verticalOffset > 0.5)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getQuotation(), ShareType.SHORT);
        }
    }

    public void checkDownSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.95) && (verticalOffset > 0.75)) {
            Stonks.plugin.configManager.QUOTATION_LIST.generate(Stonks.plugin.playerManager.get(player)).open();
        }
    }
}
