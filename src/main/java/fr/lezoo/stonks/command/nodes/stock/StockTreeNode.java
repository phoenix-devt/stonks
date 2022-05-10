package fr.lezoo.stonks.command.nodes.stock;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class StockTreeNode extends CommandTreeNode {
    public StockTreeNode(CommandTreeNode parent) {
        super(parent, "stock");

        addChild(new CreateTreeNode(this));
        addChild(new RemoveTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
