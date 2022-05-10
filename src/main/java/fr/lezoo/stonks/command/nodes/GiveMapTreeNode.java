package fr.lezoo.stonks.command.nodes;

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

public class GiveMapTreeNode extends CommandTreeNode {
    public GiveMapTreeNode(CommandTreeNode parent) {
        super(parent, "givemap");

        addParameter(Parameter.STOCK_ID);
        addParameter(Parameter.TIME_SCALE);
        addParameter(Parameter.PLAYER_OPTIONAL);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        //returning a throw usage will automatically send all the usage list to the player.
        if (args.length == 0)
            return CommandResult.THROW_USAGE;
        Stock stock = Stonks.plugin.stockManager.get(args[1]);
        if (stock == null) {
            sender.sendMessage(ChatColor.RED + "Could not find target stock");
            return CommandResult.FAILURE;
        }

        TimeScale display = null;
        try {
            display = TimeScale.valueOf(args[2].toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Could not find corresponding time scale");
            return CommandResult.FAILURE;
        }

        Player target = args.length > 3 ? Bukkit.getPlayer(args[3]) : sender instanceof Player ? (Player) sender : null;
        if (target == null && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Could not find target player");
            return CommandResult.FAILURE;
        }

        DisplayInfo displayInfo = new DisplayInfo(stock, display);
        target.getInventory().addItem(Stonks.plugin.configManager.stockMap.build(target, displayInfo));

        return CommandResult.SUCCESS;
    }
}
