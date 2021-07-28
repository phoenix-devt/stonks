package fr.lezoo.stonks.command;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.listener.temp.RemoveBoardListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

//TODO : CustomPermission and not isOp()
public class RemoveBoardCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;
        Player player = (Player) sender;
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "You don't have the right to use this command");
            return true;
        }
        player.sendMessage(ChatColor.YELLOW + "You HAve 5 seconds to break the block!");
        RemoveBoardListener removeBoardListener = new RemoveBoardListener(player);

        // After 5s the listener is unregistered with HandlerList.unregisterAll(listener)
        Bukkit.getScheduler().scheduleSyncDelayedTask(Stonks.plugin, () -> removeBoardListener.close(), 100L);
        return true;
    }
}
