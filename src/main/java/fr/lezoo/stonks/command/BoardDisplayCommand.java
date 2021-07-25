package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.quotation.Quotation;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

//TODO : Change permission isOp stonks.admin

public class BoardDisplayCommand implements CommandExecutor {
    @Override
    /**
     * The Command needs 4 arguments :
     * 1 : the id of the quotation
     * 2 : NUMBER_DATA the number of data that you want to visualize
     * 3 : the width of the board
     * 4 : the height of the board
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Problem : you're not a player!");
            return true;
        }
        Player player = (Player) sender;
        if (!(player.isOp())) {
            player.sendMessage("You dont have the right to execute this command");
            return true;
        }
        if (args.length != 4) {
            player.sendMessage(ChatColor.RED + "You need to pass 4 arguments");
            return true;
        }

        if (!Stonks.plugin.quotationManager.has(args[0])) {
            player.sendMessage(ChatColor.RED + "This quotation ID doesn't exist");
            return true;
        }

        //We look what direction the player looks at SOUTH,NORTH,WEST or EAST with scalarproduct
        final Vector direction = player.getEyeLocation().getDirection();

        BlockFace face = BlockFace.NORTH;
        double val = direction.dot(face.getDirection());

        if (direction.dot(BlockFace.WEST.getDirection()) > val) {
            face = BlockFace.WEST;
            val = direction.dot(face.getDirection());
        }
        if (direction.dot(BlockFace.SOUTH.getDirection()) > val) {
            face = BlockFace.SOUTH;
            val = direction.dot(face.getDirection());
        }
        if (direction.dot(BlockFace.EAST.getDirection()) > val) {
            face = BlockFace.EAST;
            val = direction.dot(face.getDirection());
        }

        Quotation quotation = Stonks.plugin.quotationManager.get(args[0]);
        quotation.createQuotationBoard(player, face, Integer.valueOf(args[1]), Integer.valueOf(args[2]), Integer.valueOf(args[3]));
        return true;
    }
}
