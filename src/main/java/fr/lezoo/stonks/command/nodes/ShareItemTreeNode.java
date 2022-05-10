package fr.lezoo.stonks.command.nodes;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

@Deprecated
public class ShareItemTreeNode extends CommandTreeNode {
    public ShareItemTreeNode(CommandTreeNode parent) {
        super(parent, "shareitem");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {



        // stonks shareitem <stockId> <shares> <leverage>
        // TODO






        return CommandResult.SUCCESS;
    }
}
