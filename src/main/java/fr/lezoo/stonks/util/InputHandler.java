package fr.lezoo.stonks.util;

import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.entity.Player;

/**
 * Stores all the classic inputHandlers into static variables that can be used
 */
public class InputHandler {
    //The boolean describes if the temporary listener should be closed(e.g the ChatInput or SimpleChatInput)


    public static final TriFunction<PlayerData, String, Stock, Boolean> BUY_CUSTOM_AMOUNT_HANDLER = (playerData, input, stock) -> {
        double amount;
        Player player = playerData.getPlayer();
        try {
            amount = Double.parseDouble(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_AMOUNT.format("input", input).send(player);
            return false;
        }
        playerData.buyShare(stock, ShareType.NORMAL, amount);
        return true;
    };
    public static final TriFunction<PlayerData, String, Stock, Boolean> SHORT_CUSTOM_AMOUNT_HANDLER = (playerData, input, stock) -> {
        double amount;
        Player player = playerData.getPlayer();
        try {
            amount = Double.parseDouble(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_AMOUNT.format("input", input).send(player);
            return false;
        }
        playerData.buyShare(stock, ShareType.SHORT, amount);
        return true;
    };

    public static final TriFunction<PlayerData, String, Stock, Boolean> SET_AMOUNT_HANDLER = (playerData, input, stock) -> {
        double amount;
        Player player = playerData.getPlayer();
        try {
            amount = Double.parseDouble(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_AMOUNT.format("input", input).send(player);
            return false;
        }

        playerData.getOrderInfo(stock.getId()).setAmount(amount);
        return true;
    };

    public static final TriFunction<PlayerData, String, Stock, Boolean> SET_LEVERAGE_HANDLER = (playerData, input, stock) -> {
        double amount;

        Player player = playerData.getPlayer();
        try {
            amount = Double.parseDouble(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_LEVERAGE.format("input", input).send(player);
            return false;
        }

        playerData.getOrderInfo(stock.getId()).setLeverage(amount);

        return true;
    };

    public static final TriFunction<PlayerData, String, Stock, Boolean> SET_MIN_PRICE_HANDLER = (playerData, input, stock) -> {
        double amount;
        Player player = playerData.getPlayer();
        try {
            amount = Double.parseDouble(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_MIN_PRICE.format("input", input).send(player);
            return false;
        }

        playerData.getOrderInfo(stock.getId()).setMinPrice(amount);
        return true;
    };

    public static final TriFunction<PlayerData, String, Stock, Boolean> SET_MAX_PRICE_HANDLER = (playerData, input, stock) -> {
        double amount;
        Player player = playerData.getPlayer();
        try {
            amount = Double.parseDouble(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_MAX_PRICE.format("input", input).send(player);
            return false;
        }

        playerData.getOrderInfo(stock.getId()).setMaxPrice(amount);
        return true;
    };


}

