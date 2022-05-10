package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.display.board.BoardRaycast;
import fr.lezoo.stonks.display.sign.DisplaySign;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.OrderInfo;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.Position;
import fr.lezoo.stonks.util.message.Message;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.block.data.type.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class TradingInteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        //We check if the player is left clicking
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            checkBoard(player);
            return;
        }


        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().name().contains("SIGN")) {
            Position pos = Position.from(event.getClickedBlock().getLocation());
            if (!Stonks.plugin.signManager.has(pos))
                return;
            DisplaySign sign = Stonks.plugin.signManager.get(pos);
            Stonks.plugin.configManager.STOCK_LIST.generate(PlayerData.get(player)).open();
        }
    }


    public void checkBoard(Player player) {
        BoardRaycast cast = new BoardRaycast(player);
        if (!cast.hasHit())
            return;
        checkDownSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
        checkMiddleDownSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
        checkMiddleUpSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());
        checkUpSquare(player, cast.getHit(), cast.getVerticalCoordinate(), cast.getHorizontalCoordinate());

    }


    //Does what needs to be done when the top square is touched
    public void checkUpSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.21) && (verticalOffset > 0.02)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            OrderInfo orderInfo = playerData.getOrderInfo(board.getStock().getId());

            TextComponent amountComponent = new TextComponent(Message.SET_AMOUNT_INFO.format("amount", String.valueOf(orderInfo.getAmount())).getAsString());
            amountComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stocks amountinput " + board.getStock().getId()));
            player.spigot().sendMessage(amountComponent);

            TextComponent leverageComponent = new TextComponent(Message.SET_LEVERAGE_INFO.format("leverage", String.valueOf(orderInfo.getLeverage())).getAsString());
            leverageComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stocks leverageinput " + board.getStock().getId()));
            player.spigot().sendMessage(leverageComponent);

            TextComponent minPriceComponent = new TextComponent(Message.SET_MIN_PRICE_INFO.format("min-price", String.valueOf(orderInfo.getMinPrice())).getAsString());
            minPriceComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stocks minpriceinput " + board.getStock().getId()));
            player.spigot().sendMessage(minPriceComponent);

            TextComponent maxPriceComponent = new TextComponent(Message.SET_MAX_PRICE_INFO.format("max-price", String.valueOf(orderInfo.getMaxPrice())).getAsString());
            maxPriceComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/stocks maxpriceinput " + board.getStock().getId()));
            player.spigot().sendMessage(maxPriceComponent);
        }
    }

    public void checkMiddleUpSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.45) && (verticalOffset > 0.25)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getStock(), ShareType.NORMAL);
        }
    }


    public void checkMiddleDownSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.7) && (verticalOffset > 0.5)) {
            PlayerData playerData = Stonks.plugin.playerManager.get(player);
            playerData.buyShare(board.getStock(), ShareType.SHORT);
        }
    }

    public void checkDownSquare(Player player, Board board, double verticalOffset, double horizontalOffset) {
        if ((horizontalOffset > 0.82) && (horizontalOffset < 0.98) && (verticalOffset < 0.95) && (verticalOffset > 0.75)) {
            Stonks.plugin.configManager.SPECIFIC_PORTFOLIO.generate(Stonks.plugin.playerManager.get(player), board.getStock()).open();
        }
    }
}
