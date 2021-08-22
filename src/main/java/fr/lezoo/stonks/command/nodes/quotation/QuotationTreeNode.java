package fr.lezoo.stonks.command.nodes.quotation;

import fr.lezoo.stonks.command.nodes.quotation.CreateTreeNode;
import fr.lezoo.stonks.command.nodes.quotation.RemoveTreeNode;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class QuotationTreeNode extends CommandTreeNode {
    public QuotationTreeNode(CommandTreeNode parent) {
        super(parent, "quotation");

        addChild(new CreateTreeNode(this));
        addChild(new RemoveTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
