package fr.lezoo.stonks.command.nodes.display.sign;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import fr.lezoo.stonks.listener.temp.SignDisplayEditionListener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateTreeNode extends CommandTreeNode {
    public CreateTreeNode(CommandTreeNode parent) {
        super(parent, "create");

        addParameter(Parameter.STOCK_ID);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only");
            return CommandResult.FAILURE;
        }

        if (args.length < 4)
            return CommandResult.THROW_USAGE;

        String stockId = args[3].toLowerCase();
        if (!Stonks.plugin.stockManager.has(stockId)) {
            sender.sendMessage(ChatColor.RED + "Could not find a stock with ID '" + stockId + "'");
            return CommandResult.FAILURE;
        }

        new SignDisplayEditionListener(Stonks.plugin.stockManager.get(stockId), (Player) sender, false);
        sender.sendMessage(ChatColor.YELLOW + "Please click on a sign to register a new display sign");
        sender.sendMessage(ChatColor.YELLOW + "You can relog to cancel display sign creation");
        return CommandResult.SUCCESS;
    }
}
