package fr.lezoo.stonks.command.nodes.display;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import fr.lezoo.stonks.display.board.DisplayInfo;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.TimeScale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MapTreeNode extends CommandTreeNode {
    public MapTreeNode(CommandTreeNode parent) {
        super(parent, "map");

        addParameter(Parameter.STOCK_ID);
        addParameter(Parameter.TIME_SCALE);
        addParameter(Parameter.PLAYER_OPTIONAL);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 3)
            return CommandResult.THROW_USAGE;

        Stock stock = Stonks.plugin.stockManager.get(args[2]);
        if (stock == null) {
            sender.sendMessage(ChatColor.RED + "Could not find target stock");
            return CommandResult.FAILURE;
        }

        TimeScale display;
        try {
            display = TimeScale.valueOf(args[3].toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Could not find corresponding time scale");
            return CommandResult.FAILURE;
        }

        Player target = args.length > 4 ? Bukkit.getPlayer(args[4]) : sender instanceof Player ? (Player) sender : null;
        if (target == null && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Could not find target player");
            return CommandResult.FAILURE;
        }

        DisplayInfo displayInfo = new DisplayInfo(stock, display);
        target.getInventory().addItem(Stonks.plugin.configManager.stockMap.build(target, displayInfo));

        return CommandResult.SUCCESS;
    }
}
