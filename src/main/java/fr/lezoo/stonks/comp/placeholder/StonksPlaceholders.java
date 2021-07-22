package fr.lezoo.stonks.comp.placeholder;

import fr.lezoo.stonks.Stonks;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
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
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // TODO
        return "";
    }
}
