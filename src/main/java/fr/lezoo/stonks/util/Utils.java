package fr.lezoo.stonks.util;

import fr.lezoo.stonks.Stonks;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;

public class Utils {
    public static DecimalFormat singleDigit = new DecimalFormat("0.#");
    public static DecimalFormat fourDigits = new DecimalFormat("0.####");

    /**
     * Transforms 'badly-formatted ENUM name' into 'BADLY_FORMATTED_ENUM_NAME'
     *
     * @param str String input
     * @return String formatted for enum fields
     */
    public static String enumName(String str) {
        return str == null ? "" : str.toUpperCase().replace(" ", "_").replace("-", "_");
    }

    public static NamespacedKey namespacedKey(String str) {
        return new NamespacedKey(Stonks.plugin, str);
    }

    /**
     * @param x        Number to truncate
     * @param decimals Amount of decimals
     * @return Double truncated to X decimals
     */
    public static double truncate(double x, int decimals) {
        double pow = Math.pow(10, decimals);
        return Math.floor(x * pow) / pow;
    }

    /**
     * @return The direction to follow to place the item frames
     * on a quotation board
     */
    @Deprecated
    public static Vector getItemFrameDirection(BlockFace blockFace) {
        switch (blockFace) {
            case NORTH:
                return BlockFace.EAST.getDirection();
            case EAST:
                return BlockFace.SOUTH.getDirection();
            case SOUTH:
                return BlockFace.WEST.getDirection();
            case WEST:
                return BlockFace.NORTH.getDirection();
            default:
                throw new IllegalArgumentException("Could not match any direction");
        }
    }

    public static String formatRate(double growthRate) {
        return formatGain(growthRate) + "%";
    }

    public static String formatGain(double d) {
        if (d == 0)
            return ChatColor.WHITE + "0";

        DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;
        return d < 0 ? ChatColor.RED + format.format(d) : ChatColor.GREEN + "+" + format.format(d);
    }
}
