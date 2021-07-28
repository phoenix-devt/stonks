package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.util.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class QuotationsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return false;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("stonks.quotation-list")) {
            Message.NOT_ENOUGH_PERMISSIONS.format().send(player);
            return true;
        }

        PlayerData playerData = PlayerData.get(player);
        Stonks.plugin.configManager.QUOTATION_LIST.generate(playerData).open();
        return true;
    }
}
