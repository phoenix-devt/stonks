package fr.lezoo.stonks.command.nodes.display.sign;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.command.CommandSender;

public class SignTreeNode extends CommandTreeNode {
    public SignTreeNode(CommandTreeNode parent) {
        super(parent, "sign");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }
}
