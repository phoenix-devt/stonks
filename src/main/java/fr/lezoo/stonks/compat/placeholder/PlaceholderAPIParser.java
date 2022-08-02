package fr.lezoo.stonks.compat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

public class PlaceholderAPIParser implements PlaceholderParser {

    @Override
    public String parse(Player player, String input) {

        input = ChatColor.translateAlternateColorCodes('&', input);
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}
