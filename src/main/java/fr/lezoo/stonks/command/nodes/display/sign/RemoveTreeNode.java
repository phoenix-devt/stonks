package fr.lezoo.stonks.command.nodes.display.sign;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.listener.temp.SignDisplayEditionListener;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RemoveTreeNode extends CommandTreeNode {
    public RemoveTreeNode(CommandTreeNode parent) {
        super(parent, "remove");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only");
            return CommandResult.FAILURE;
        }

        new SignDisplayEditionListener(null, (Player) sender, true);
        sender.sendMessage(ChatColor.YELLOW + "Please click on a display sign to delete it");
        sender.sendMessage(ChatColor.YELLOW + "You can relog to cancel display sign removal");
        return CommandResult.SUCCESS;
    }
}
