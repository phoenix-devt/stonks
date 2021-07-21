package fr.lezoo.stonks.comp.placeholder;

import org.bukkit.entity.Player;

/**
 * Interface between any placeholder plugin and Stonks
 *
 * @author jules
 */
public interface PlaceholderParser {

    /**
     * @param player Player to parse placeholders with
     * @param input  String input
     * @return String input with parsed placeholders
     */
    public String parse(Player player, String input);
}
