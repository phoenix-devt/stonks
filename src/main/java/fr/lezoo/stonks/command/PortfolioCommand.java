package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PortfolioCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return false;
        }

        Player player = (Player) sender;
        Stonks.plugin.configManager.PORTFOLIO_LIST.generate(PlayerData.get(player)).open();
        return true;
    }
}
