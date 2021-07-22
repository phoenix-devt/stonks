package fr.lezoo.stonks.command;

import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StonksCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;


        if (args.length > 0 && args[0].equals("stonks")) {

            // debug


            return true;
        }


        Player player = (Player) sender;
        List<QuotationInfo> quot = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            quot.add(new QuotationInfo(System.currentTimeMillis() + 10000 * i, Math.log(1 + i)));
        Quotation quotation = new Quotation("aaa", "ooo", "hiiii", quot);
        player.getInventory().addItem(quotation.createQuotationMap(10));
        return true;
    }
}
