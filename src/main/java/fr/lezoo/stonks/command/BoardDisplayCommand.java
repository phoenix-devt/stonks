package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BoardDisplayCommand implements CommandExecutor {

    /**
     * The Command needs 4 arguments :
     * 1 : the id of the quotation
     * 2 : NUMBER_DATA the number of data that you want to visualize
     * 3 : the width of the board
     * 4 : the height of the board
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("stonks.admin")) {
            Message.NOT_ENOUGH_PERMISSIONS.format().send(player);
            return true;
        }

        if (args.length != 4) {
            player.sendMessage(ChatColor.RED + "Usage: /boarddisplay <quotation> <data-number> <width> <height>");
            return true;
        }

        if (!Stonks.plugin.quotationManager.has(args[0])) {
            player.sendMessage(ChatColor.RED + "Could not find a quotation with ID '" + args[0] + "'");
            return true;
        }

        // Find the player's direction using max value of scalar product
        final Vector direction = player.getEyeLocation().getDirection();

        BlockFace face = BlockFace.NORTH;
        double val = direction.dot(face.getDirection());

        for (BlockFace checked : new BlockFace[]{BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST})
            if (direction.dot(checked.getDirection()) > val) {
                val = direction.dot(checked.getDirection());
                face = checked;
            }

        int numberData = 0;
        try {
            numberData = Integer.parseInt(args[1]);
        } catch (IllegalArgumentException exception) {
            player.sendMessage(ChatColor.RED + "Could not parse data number '" + args[1] + "'");
            return false;
        }

        int width = 0;
        try {
            width = Integer.parseInt(args[2]);
        } catch (IllegalArgumentException exception) {
            player.sendMessage(ChatColor.RED + "Could not parse width '" + args[2] + "'");
            return false;
        }

        int height = 0;
        try {
            height = Integer.parseInt(args[3]);
        } catch (IllegalArgumentException exception) {
            player.sendMessage(ChatColor.RED + "Could not parse height '" + args[3] + "'");
            return false;
        }

        // Work with integers instead to simplify calculations
        Location location = new Location(player.getWorld(), Math.floor(player.getLocation().getX()), Math.floor(player.getLocation().getY()), Math.floor(player.getLocation().getX()));
        Quotation quotation = Stonks.plugin.quotationManager.get(args[0]);
        quotation.createQuotationBoard(false, player.getLocation().add(face.getDirection()), face, numberData, width, height);
        return true;
    }
}
