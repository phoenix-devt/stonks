package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;


//TODO : Change permission isOp stonks.admin

public class BoardDisplayCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender,Command command,String label,String[] args) {
        if(!(sender instanceof Player))
            return false;
        Player player =(Player) sender;
        if(!(player.isOp())) {
            player.sendMessage("You dont have the right to execute this command");
            return true;
        }
        if(args.length !=3) {
            player.sendMessage("You need to pass 3 arguments");
            return true;
        }
        /*
        North : negative z
        South :

         */







        return false;
    }
}
