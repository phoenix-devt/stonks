package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.share.Share;
import fr.lezoo.stonks.api.util.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RedeemDividendsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return false;
        }

        Player player = (Player) sender;
        PlayerData playerData = PlayerData.get(player);

        double totalDividends = 0;

        for (Share share : playerData.getAllShares()) {
            double wallet = share.getWallet();
            if (wallet != 0) {
                totalDividends += wallet;
                share.setWallet(0);
            }
        }

        if (totalDividends != 0) {
            Stonks.plugin.economy.depositPlayer(player, totalDividends);
            Message.CLAIM_DIVIDENDS.format("amount", Stonks.plugin.configManager.stockPriceFormat.format(totalDividends)).send(player);
            return true;
        }

        Message.NO_DIVIDENDS_TO_CLAIM.format().send(player);
        return true;
    }
}
