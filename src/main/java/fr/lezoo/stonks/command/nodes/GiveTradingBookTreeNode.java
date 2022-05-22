package fr.lezoo.stonks.command.nodes;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Deprecated
public class GiveTradingBookTreeNode extends CommandTreeNode {
    public GiveTradingBookTreeNode(CommandTreeNode parent) {
        super(parent, "givetradingbook");

        addParameter(Parameter.PLAYER_OPTIONAL);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {

        Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : sender instanceof Player ? (Player) sender : null;
        if (target == null && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Could not find target player");
            return CommandResult.FAILURE;
        }

        target.getInventory().addItem(Stonks.plugin.configManager.tradingBook.build(target, null));
        return CommandResult.SUCCESS;
    }
}
