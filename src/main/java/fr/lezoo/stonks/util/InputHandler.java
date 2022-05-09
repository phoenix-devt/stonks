package fr.lezoo.stonks.util;

import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.entity.Player;

/**
 * Stores all the classic inputHandlers into static variables that can be used
 */
public class InputHandler {
    //The boolean describes if the temporary listener should be closed(e.g the ChatInput or SimpleChatInput)


    public static final TriFunction<PlayerData, String, Quotation, Boolean> BUY_CUSTOM_AMOUNT_HANDLER = (playerData, input, quotation) -> {
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
        playerData.buyShare(quotation, ShareType.SHORT, amount);
        return true;
    };
    public static final TriFunction<PlayerData, String, Quotation, Boolean> SHORT_CUSTOM_AMOUNT_HANDLER = (playerData, input, quotation) -> {
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
        playerData.buyShare(quotation, ShareType.SHORT, amount);
        return true;
    };

    public static final TriFunction<PlayerData, String, Quotation, Boolean> SET_AMOUNT_HANDLER = (playerData, input, quotation) -> {
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

        playerData.getOrderInfo(quotation.getId()).setAmount(amount);
        return true;
    };

    public static final TriFunction<PlayerData, String, Quotation, Boolean> SET_LEVERAGE_HANDLER = (playerData, input, quotation) -> {
        int amount;

        Player player = playerData.getPlayer();
        try {
            amount = Integer.parseInt(input);
        } catch (IllegalArgumentException exception) {
            Message.NOT_VALID_NUMBER.format("input", input).send(player);
            return false;
        }

        if (amount <= 0) {
            Message.NOT_VALID_LEVERAGE.format("input", input).send(player);
            return false;
        }

        playerData.getOrderInfo(quotation.getId()).setLeverage(amount);

        return true;
    };

    public static final TriFunction<PlayerData, String, Quotation, Boolean> SET_MIN_PRICE_HANDLER = (playerData, input, quotation) -> {
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

        playerData.getOrderInfo(quotation.getId()).setMinPrice(amount);
        return true;
    };

    public static final TriFunction<PlayerData, String, Quotation, Boolean> SET_MAX_PRICE_HANDLER = (playerData, input, quotation) -> {
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

        playerData.getOrderInfo(quotation.getId()).setMaxPrice(amount);
        return true;
    };


}

