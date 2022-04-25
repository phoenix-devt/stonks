package fr.lezoo.stonks.command.nodes.debug;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.display.sign.DisplaySign;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class UpdateSignsTreeNode extends CommandTreeNode {
    public UpdateSignsTreeNode(CommandTreeNode parent) {
        super(parent, "updatesigns");
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        Stonks.plugin.configManager.reload();
        for (DisplaySign sign : Stonks.plugin.signManager.getActive())
            sign.update();
        sender.sendMessage(ChatColor.BLUE + "Updated all display signs (" + Stonks.plugin.signManager.getActive().size() + ")");
        return CommandResult.SUCCESS;
    }
}
