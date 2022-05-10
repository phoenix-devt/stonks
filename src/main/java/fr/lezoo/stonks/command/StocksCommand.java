package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.util.InputHandler;
import fr.lezoo.stonks.util.SimpleChatInput;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StocksCommand implements CommandExecutor {
    public static final String
            SET_AMOUNT_SECRET = "amountinput",
            SET_LEVERAGE_SECRET = "leverageinput",
            SET_MIN_PRICE_SECRET = "minpriceinput",
            SET_MAX_PRICE_SECRET = "maxpriceinput";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return false;
        }

        // Secret commands for clickable messages
        if (args.length > 1) {
            Stock stock;
            try {
                stock = Stonks.plugin.stockManager.get(args[1]);
            } catch (RuntimeException exception) {
                return true;
            }

            Player player = (Player) sender;
            PlayerData playerData = PlayerData.get(player);
            if (playerData.isOnChatInput())
                return true;

            if (args[0].equalsIgnoreCase(SET_AMOUNT_SECRET)) {
                Message.SET_AMOUNT_ASK.format().send(sender);
                new SimpleChatInput(playerData, stock, InputHandler.SET_AMOUNT_HANDLER);
                return true;
            }

            if (args[0].equalsIgnoreCase(SET_LEVERAGE_SECRET)) {
                Message.SET_LEVERAGE_ASK.format().send(sender);
                new SimpleChatInput(playerData, stock, InputHandler.SET_LEVERAGE_HANDLER);
                return true;
            }

            if (args[0].equalsIgnoreCase(SET_MIN_PRICE_SECRET)) {
                Message.SET_MIN_PRICE_ASK.format().send(sender);
                new SimpleChatInput(playerData, stock, InputHandler.SET_MIN_PRICE_HANDLER);
                return true;
            }

            if (args[0].equalsIgnoreCase(SET_MAX_PRICE_SECRET)) {
                Message.SET_MAX_PRICE_ASK.format().send(sender);
                new SimpleChatInput(playerData, stock, InputHandler.SET_MAX_PRICE_HANDLER);
                return true;
            }
        }

        Player player = (Player) sender;
        if (!player.hasPermission("stonks.stock-list")) {
            Message.NOT_ENOUGH_PERMISSIONS.format().send(player);
            return true;
        }

        PlayerData playerData = PlayerData.get(player);
        Stonks.plugin.configManager.STOCK_LIST.generate(playerData).

                open();
        return true;
    }
}
