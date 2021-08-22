package fr.lezoo.stonks.command.nodes;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import fr.lezoo.stonks.display.DisplayInfo;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveMapTreeNode extends CommandTreeNode {
    public GiveMapTreeNode(CommandTreeNode parent) {
        super(parent, "givemap");

        addParameter(Parameter.QUOTATION_ID);
        addParameter(Parameter.TIME_SCALE);
        addParameter(Parameter.PLAYER_OPTIONAL);
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {

        Quotation quotation = Stonks.plugin.quotationManager.get(args[1]);
        if (quotation == null) {
            sender.sendMessage(ChatColor.RED + "Could not find target quotation");
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

        DisplayInfo displayInfo = new DisplayInfo(quotation, display);
        target.getInventory().addItem(Stonks.plugin.configManager.quotationMap.build(target, displayInfo));

        return CommandResult.SUCCESS;
    }
}
