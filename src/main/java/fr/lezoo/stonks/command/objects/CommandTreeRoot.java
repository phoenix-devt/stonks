package fr.lezoo.stonks.command.objects;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public abstract class CommandTreeRoot extends CommandTreeNode implements CommandExecutor, TabCompleter {
    private final String permission;

    /**
     * First class called when creating a command tree
     *
     * @param id         The command tree root id
     * @param permission The eventual permission the player needs to have in order to
     *                   use the command
     */
    public CommandTreeRoot(String id, String permission) {
        super(null, id);

        this.permission = permission;
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        return CommandResult.THROW_USAGE;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission))
            return false;

        CommandTreeNode explorer = new CommandTreeExplorer(this, args).getNode();
        if (explorer.execute(sender, args) == CommandResult.THROW_USAGE)
            explorer.calculateUsageList().forEach(str -> sender.sendMessage(ChatColor.YELLOW + "/" + str));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(permission))
            return new ArrayList<>();

        List<String> list = new CommandTreeExplorer(this, args).calculateTabCompletion();
        return args[args.length - 1].isEmpty() ? list
                : list.stream().filter(string -> string.toLowerCase().startsWith(args[args.length - 1].toLowerCase())).collect(Collectors.toList());
    }
}
