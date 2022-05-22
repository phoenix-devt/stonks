package fr.lezoo.stonks.compat.placeholder;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.share.Share;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class StonksPlaceholders extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "stonks";
    }

    @Override
    public @NotNull String getAuthor() {
        return "LeZoo";
    }

    @Override
    public @NotNull String getVersion() {
        return Stonks.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (!offlinePlayer.isOnline())
            return "?";

        Player player = offlinePlayer.getPlayer();
        PlayerData playerData = PlayerData.get(player);

        if (params.startsWith("shares_")) {
            String id = params.substring(7);
            if (!Stonks.plugin.stockManager.has(id))
                return "?";

            return Double.toString(playerData.countShares(Stonks.plugin.stockManager.get(id)));
        }

        if (params.startsWith("share_money_")) {
            String id = params.substring(12);
            if (!Stonks.plugin.stockManager.has(id))
                return "?";

            double sum = 0;
            for (Share share : playerData.getShares(Stonks.plugin.stockManager.get(id)))
                sum += share.getCloseEarning(0);
            return String.valueOf(sum);
        }

        if (params.startsWith("portfolio_money")) {
            double sum = 0;
            for (Stock stock : Stonks.plugin.stockManager.getStocks())
                for (Share share : playerData.getShares(stock))
                    sum += share.getCloseEarning(0);
            return String.valueOf(sum);
        }

        return null;
    }
}
