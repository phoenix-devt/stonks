package fr.lezoo.stonks.command.nodes;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ReloadTreeNode extends CommandTreeNode {
    public ReloadTreeNode(CommandTreeNode parent) {
        super(parent, "reload");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        Stonks.plugin.configManager.reload();
        sender.sendMessage(ChatColor.YELLOW + "Stonks " + Stonks.plugin.getDescription().getVersion() + " reloaded.");
        return CommandResult.SUCCESS;
    }
}
