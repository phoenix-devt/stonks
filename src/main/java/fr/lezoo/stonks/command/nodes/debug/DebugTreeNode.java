package fr.lezoo.stonks.command.nodes.debug;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class DebugTreeNode extends CommandTreeNode {
    public DebugTreeNode(CommandTreeNode parent) {
        super(parent, "debug");

        addChild(new UpdateSignsTreeNode(this));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
