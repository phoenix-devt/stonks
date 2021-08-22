package fr.lezoo.stonks.command.nodes.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.listener.temp.RemoveBoardListener;
import org.bukkit.Bukkit;
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
        player.sendMessage(ChatColor.YELLOW + "You have 30 seconds to break one of the item frames of a board.");
        player.sendMessage(ChatColor.YELLOW + "This will have the effect of unregistering the entire board.");
        RemoveBoardListener removeBoardListener = new RemoveBoardListener(player);

        // After 30s the listener is unregistered with HandlerList.unregisterAll(listener)
        Bukkit.getScheduler().scheduleSyncDelayedTask(Stonks.plugin, () -> removeBoardListener.close(), 20 * 30);
        return null;
    }
}
