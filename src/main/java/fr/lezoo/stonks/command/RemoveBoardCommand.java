package fr.lezoo.stonks.command;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.listener.RemoveBoardListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

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
        Bukkit.getPluginManager().registerEvents(removeBoardListener, Stonks.plugin);

        //After 5 s the listener is unregistered with Handler.unregisterAll(listener)
        Bukkit.getScheduler().scheduleSyncDelayedTask(Stonks.plugin, () -> {
                    HandlerList.unregisterAll(removeBoardListener);
                    player.sendMessage(ChatColor.RED + "You didn't break the block in time");
                }
                , 100L);
        return true;
    }
}
