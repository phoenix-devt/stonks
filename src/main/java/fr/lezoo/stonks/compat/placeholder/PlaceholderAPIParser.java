package fr.lezoo.stonks.compat.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderAPIParser implements PlaceholderParser {

    @Override
    public String parse(Player player, String input) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}
