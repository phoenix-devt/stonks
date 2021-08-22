package fr.lezoo.stonks.command.nodes.display.board;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class BoardTreeNode extends CommandTreeNode {
    public BoardTreeNode(CommandTreeNode parent) {
        super(parent, "board");

        addChild(new CreateTreeNode(this));
        addChild(new RemoveTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
