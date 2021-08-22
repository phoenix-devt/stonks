package fr.lezoo.stonks.command.nodes.display;

import fr.lezoo.stonks.command.nodes.display.board.BoardTreeNode;
import fr.lezoo.stonks.command.nodes.display.sign.SignTreeNode;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class DisplayTreeNode extends CommandTreeNode {
    public DisplayTreeNode(CommandTreeNode parent) {
        super(parent, "display");

        addChild(new SignTreeNode(this));
        addChild(new BoardTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
