package fr.lezoo.stonks.util;

import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.share.OrderInfo;
import fr.lezoo.stonks.util.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.util.function.BiFunction;

/**
 * Stores all the classic inputHandlers into static variables that can be used
 */
public class InputHandler {
    //The boolean describes if the temporary listener should be closed(e.g the ChatInput or SimpleChatInput)

    public static final BiFunction<PlayerData, String, Boolean> SET_PARAMETER_HANDLER = (playerData, input) -> {
        Quotation quotation=playerData.getCurrentQuotation();
        OrderInfo orderInfo=playerData.getOrderInfo(quotation.getId());
        int number;
        try {
            number = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            Message.NOT_VALID_NUMBER.format("input", input).send(playerData.getPlayer());
            return false;
        }
        switch (number) {
            case 1: {
                Message.SET_LEVERAGE_ASK.format().send(playerData.getPlayer());
                //We create the ChatInput to listen for the leverage and this chat input will call back
                // the SET_PARAMETER_HANDLER method later giving the message before
                new SimpleChatInput(playerData, InputHandler.SET_LEVERAGE_HANDLER,
                        //What is done when then Chat Input Handler is closed
                        () -> {
                    Message.SET_PARAMETER_ASK.format("leverage", "\n" + orderInfo.getLeverage(),
                            "amount", orderInfo.hasAmount() ? "\n" + orderInfo.getAmount() : "",
                            "min-price", orderInfo.hasMinPrice() ? "\n" + orderInfo.getMinPrice() : "",
                            "max-price", orderInfo.hasMaxPrice() ? "\n" + orderInfo.getMaxPrice() : "").send(playerData.getPlayer());

                    new SimpleChatInput(playerData, InputHandler.SET_PARAMETER_HANDLER);
                });
                return true;

            }
            case 2: {
                Message.SET_AMOUNT_ASK.format().send(playerData.getPlayer());
                new SimpleChatInput(playerData, InputHandler.SET_AMOUNT_HANDLER,
                        () -> {
                    Message.SET_PARAMETER_ASK.format("leverage", "\n" + orderInfo.getLeverage(),
                            "amount", orderInfo.hasAmount() ? "\n" + orderInfo.getAmount() : "",
                            "min-price", orderInfo.hasMinPrice() ? "\n" + orderInfo.getMinPrice() : "",
                            "max-price", orderInfo.hasMaxPrice() ? "\n" + orderInfo.getMaxPrice() : "").send(playerData.getPlayer());

                    new SimpleChatInput(playerData, InputHandler.SET_PARAMETER_HANDLER);
                });
                return true;

            }
            case 3: {
                Message.SET_MIN_PRICE_ASK.format().send(playerData.getPlayer());
                new SimpleChatInput(playerData, InputHandler.SET_MIN_PRICE_HANDLER, () -> {
                    Message.SET_PARAMETER_ASK.format("leverage", "\n" + orderInfo.getLeverage(),
                            "amount", orderInfo.hasAmount() ? "\n" + orderInfo.getAmount() : "",
                            "min-price", orderInfo.hasMinPrice() ? "\n" + orderInfo.getMinPrice() : "",
                            "max-price", orderInfo.hasMaxPrice() ? "\n" + orderInfo.getMaxPrice() : "").send(playerData.getPlayer());

                    new SimpleChatInput(playerData, InputHandler.SET_PARAMETER_HANDLER);
                });

                return true;

            }
            case 4: {
                Message.SET_MAX_PRICE_ASK.format().send(playerData.getPlayer());
                new SimpleChatInput(playerData, InputHandler.SET_MAX_PRICE_HANDLER, () -> {
                    Message.SET_PARAMETER_ASK.format("leverage", "\n" + orderInfo.getLeverage(),
                            "amount", orderInfo.hasAmount() ? "\n" + orderInfo.getAmount() : "",
                            "min-price", orderInfo.hasMinPrice() ? "\n" + orderInfo.getMinPrice() : "",
                            "max-price", orderInfo.hasMaxPrice() ? "\n" + orderInfo.getMaxPrice() : "").send(playerData.getPlayer());

                    new SimpleChatInput(playerData, InputHandler.SET_PARAMETER_HANDLER);
                });
                return true;

            }
            case 100:
                Message.SAVE_PARAMETER.format().send(playerData.getPlayer());
                //We stop the dialog with the player because he jsut want to save the data
                return true;
        }
        Message.NOT_VALID_NUMBER.format("input", input).send(playerData.getPlayer());
        return false;
    };

    public static final BiFunction<PlayerData, String, Boolean> SET_LEVERAGE_HANDLER = (playerData, input) -> {
        Quotation quotation = playerData.getCurrentQuotation();
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

        playerData.getOrderInfo(quotation.getId()).setLeverage(amount);

        return true;
    };


    public static final BiFunction<PlayerData, String, Boolean> SET_AMOUNT_HANDLER = (playerData, input) -> {
        Quotation quotation = playerData.getCurrentQuotation();
        Validate.notNull(quotation, "The current quotation of " + playerData.getPlayer().getName() + "is null");
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
    public static final BiFunction<PlayerData, String, Boolean> SET_MIN_PRICE_HANDLER = (playerData, input) -> {
        Quotation quotation = playerData.getCurrentQuotation();
        Validate.notNull(quotation, "The current quotation of " + playerData.getPlayer().getName() + "is null");
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

    public static final BiFunction<PlayerData, String, Boolean> SET_MAX_PRICE_HANDLER = (playerData, input) -> {
        Quotation quotation = playerData.getCurrentQuotation();
        Validate.notNull(quotation, "The current quotation of " + playerData.getPlayer().getName() + "is null");
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

