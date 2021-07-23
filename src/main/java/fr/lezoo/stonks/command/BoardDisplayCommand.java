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
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;


//TODO : Change permission isOp stonks.admin

public class BoardDisplayCommand implements CommandExecutor {
    @Override
    /**
     * The Command needs 4 arguments :
     * 1 : the id of the quotation
     * 2 : the Distance it will be created from the Player
     * 3 : the size of the panel
     * 4 : the Direction SOUTH,EAST,WEST,EAST from where you will go
     * 5 : NUMBER_DATA the number of data that you want to visualize
     */
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if(!(sender instanceof Player))
            return false;
        Player player =(Player) sender;
        if(!(player.isOp())) {
            player.sendMessage("You dont have the right to execute this command");
            return true;
        }
        if(args.length !=5) {
            player.sendMessage(ChatColor.RED+"You need to pass 4 arguments");
            return true;
        }
        //With BlockFace enum we get direction corresponding to south,east,north...
        if(!(Arrays.asList("NORTH","SOUTH","EAST","WEST").contains(args[1]))) {
            player.sendMessage(ChatColor.RED+"First argument can only be : NORTH,SOUTH,UP,DOWN,EAST,WEST");
            return true;
        }
        if(!Stonks.plugin.quotationManager.hasId(args[0])) {
            player.sendMessage(ChatColor.RED+"This quotation ID doesn't exist");
        }
        Quotation quotation = Stonks.plugin.quotationManager.get(args[0]);
        quotation.createQuotationBoard(player,BlockFace.valueOf(args[3]),Integer.valueOf(args[1]),Integer.valueOf(args[4]),Integer.valueOf(args[2]));
        return true;
    }
}
