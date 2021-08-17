package fr.lezoo.stonks.compat.placeholder;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Only parses & chat colors and hex colors
 */
public class DefaultPlaceholderParser implements PlaceholderParser {
    private final Pattern PATTERN = Pattern.compile("<#([A-Fa-f0-9]){6}>");

    @Override
    public String parse(Player player, String input) {

        // Parse chat colors
        input = ChatColor.translateAlternateColorCodes('&', input);

        // Parse hex colors
        Matcher match = PATTERN.matcher(input);

        while (match.find()) {
            String color = input.substring(match.start(), match.end());
            input = input.replace(color, "" + ChatColor.of('#' + match.group(2)));
            match = PATTERN.matcher(input);
        }

        return input;
    }
}
