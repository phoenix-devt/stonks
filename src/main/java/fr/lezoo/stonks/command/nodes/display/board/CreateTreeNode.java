package fr.lezoo.stonks.command.nodes.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.NumericalParameter;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CreateTreeNode extends CommandTreeNode {
    public CreateTreeNode(CommandTreeNode parent) {
        super(parent, "create");

        addParameter(Parameter.QUOTATION_ID);
        addParameter(Parameter.TIME_SCALE);
        addParameter(new NumericalParameter("<width>", 4, 5, 6, 7, 8, 9, 10));
        addParameter(new NumericalParameter("<height>", 4, 5, 6, 7, 8, 9, 10));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command is for players only");
            return CommandResult.FAILURE;
        }

        if (args.length < 7)
            return CommandResult.THROW_USAGE;

        String quotationId = args[3].toLowerCase();
        if (!Stonks.plugin.quotationManager.has(quotationId)) {
            sender.sendMessage(ChatColor.RED + "Could not find a quotation with ID '" + quotationId + "'");
            return CommandResult.FAILURE;
        }

        // Find the player's direction using max value of scalar product
        Player player = (Player) sender;
        final Vector direction = player.getEyeLocation().getDirection();

        BlockFace face = BlockFace.NORTH;
        double val = direction.dot(face.getDirection());
        for (BlockFace checked : new BlockFace[]{BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST})
            if (direction.dot(checked.getDirection()) > val) {
                val = direction.dot(checked.getDirection());
                face = checked;
            }

        TimeScale time = null;
        try {
            time = TimeScale.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Available time scales: QUARTERHOUR, HOUR, DAY, WEEK, MONTH, YEAR");
            return CommandResult.FAILURE;
        }

        int width = 0;
        try {
            width = Integer.parseInt(args[5]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Could not parse width '" + args[5] + "'");
            return CommandResult.FAILURE;
        }

        int height = 0;
        try {
            height = Integer.parseInt(args[6]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Could not parse height '" + args[6] + "'");
            return CommandResult.FAILURE;
        }

        // Work with integers instead to simplify calculations
        Location location = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
        Quotation quotation = Stonks.plugin.quotationManager.get(quotationId);
        quotation.createQuotationBoard(false, location.add(face.getDirection().multiply(2)), face, time, width, height);
        return CommandResult.SUCCESS;
    }
}
