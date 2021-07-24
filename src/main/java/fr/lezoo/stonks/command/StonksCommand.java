package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StonksCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        if (args.length > 0 && args[0].equals("stonks")) {

            // debug
            PlayerData playerData = PlayerData.get(player);
            Stonks.plugin.configManager.QUOTATION_LIST.generate(playerData).open();

            return true;
        }

        Quotation quotation = Stonks.plugin.quotationManager.getQuotations().stream().findAny().get();

        return true;
    }
}
