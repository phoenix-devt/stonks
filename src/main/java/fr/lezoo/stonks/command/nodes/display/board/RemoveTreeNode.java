package fr.lezoo.stonks.command.nodes.display.board;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.display.board.BoardRaycast;
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

        Player player = (Player) sender;

        BoardRaycast cast = new BoardRaycast(player);
        if (!cast.hasHit()) {
            player.sendMessage(ChatColor.RED + "You are not looking at any display board");
            return CommandResult.FAILURE;
        }

        cast.getHit().remove();
        player.sendMessage(ChatColor.RED + "Successfully destroyed display board of quotation '" + cast.getHit().getQuotation().getId() + "'");
        return CommandResult.SUCCESS;
    }
}
