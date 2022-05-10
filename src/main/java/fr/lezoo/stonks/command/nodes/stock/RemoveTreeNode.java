package fr.lezoo.stonks.command.nodes.stock;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RemoveTreeNode extends CommandTreeNode {
    public RemoveTreeNode(CommandTreeNode parent) {
        super(parent, "remove");

        addParameter(Parameter.STOCK_ID);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        String format = args[2].toLowerCase();

        if (!Stonks.plugin.stockManager.has(format)) {
            sender.sendMessage(ChatColor.RED + "There is no stock with this ID");
            return CommandResult.FAILURE;
        }

        Stonks.plugin.stockManager.remove(format);
        return CommandResult.SUCCESS;
    }
}
