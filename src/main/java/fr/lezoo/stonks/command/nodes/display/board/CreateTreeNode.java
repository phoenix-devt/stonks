package fr.lezoo.stonks.command.nodes.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.NumericalParameter;
import fr.lezoo.stonks.command.objects.parameter.Parameter;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
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

        // Find the player's INVERSE direction using min value of scalar product
        Player player = (Player) sender;
        Vector playerDirection = player.getEyeLocation().getDirection();

        // Find the block the player is looking at
        Block block = player.getTargetBlock(null, 10);
        if (block == null || block.getType().equals(Material.AIR)) {
            player.sendMessage(ChatColor.RED + "Please point towards a block less than 10 units away");
            return CommandResult.FAILURE;
        }

        BlockFace face = BlockFace.NORTH;
        double val = playerDirection.dot(face.getDirection());
        for (BlockFace checked : new BlockFace[]{BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST})
            if (playerDirection.dot(checked.getDirection()) < val) {
                val = playerDirection.dot(checked.getDirection());
                face = checked;
            }

        TimeScale time;
        try {
            time = TimeScale.valueOf(args[4].toUpperCase());
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Available time scales: HOUR, DAY, WEEK, MONTH, YEAR");
            return CommandResult.FAILURE;
        }

        int width;
        try {
            width = Integer.parseInt(args[5]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Could not parse width '" + args[5] + "'");
            return CommandResult.FAILURE;
        }

        int height;
        try {
            height = Integer.parseInt(args[6]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Could not parse height '" + args[6] + "'");
            return CommandResult.FAILURE;
        }

        // Work with integers instead to simplify calculations
        Quotation quotation = Stonks.plugin.quotationManager.get(quotationId);
        new Board(quotation, width, height, block.getLocation(), time, face);
        return CommandResult.SUCCESS;
    }
}
