package fr.lezoo.stonks.comp.placeholder;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
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

        if (params.equals("leverage"))
            return Double.toString(playerData.getLeverage());

        if (params.startsWith("shares_")) {
            String id = params.substring(7);
            if (!Stonks.plugin.quotationManager.has(id))
                return "?";

            return Double.toString(playerData.countShares(Stonks.plugin.quotationManager.get(id)));
        }

        return "";
    }
}
