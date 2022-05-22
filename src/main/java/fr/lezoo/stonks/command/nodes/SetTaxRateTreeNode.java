package fr.lezoo.stonks.command.nodes;

import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import fr.lezoo.stonks.player.PlayerData;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetTaxRateTreeNode extends CommandTreeNode {
    public SetTaxRateTreeNode(CommandTreeNode parent) {
        super(parent, "taxrate");

        addParameter(Parameter.PLAYER);
        addParameter(new Parameter("<taxRate>", (explorer, list) -> {
            list.add("0.1");
            list.add("0.2");
            list.add("0.3");
        }));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 2)
            return CommandResult.THROW_USAGE;

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Could not find target player");
            return CommandResult.FAILURE;
        }

        if (args.length == 2) {
            sender.sendMessage(ChatColor.YELLOW + "Tax rate of " + target.getName() + " is set to " + PlayerData.get(target).getTaxRate());
            return CommandResult.SUCCESS;
        }

        double rate;
        try {
            rate = Double.valueOf(args[2]);
            Validate.isTrue(rate >= 0 && rate <= 1);
        } catch (RuntimeException exception) {
            sender.sendMessage(ChatColor.RED + "Not a valid tax rate");
            return CommandResult.FAILURE;
        }

        PlayerData.get(target).setTaxRate(rate);
        sender.sendMessage(ChatColor.YELLOW + "Set tax rate of " + target.getName() + " to " + rate);
        return CommandResult.SUCCESS;
    }
}
