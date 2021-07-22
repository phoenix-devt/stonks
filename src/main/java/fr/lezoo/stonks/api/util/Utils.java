package fr.lezoo.stonks.api.util;

import java.text.DecimalFormat;

public class Utils {

    public static DecimalFormat singleDigit = new DecimalFormat("0.#");

    /**
     * Transforms 'badly-formatted ENUM name' into 'BADLY_FORMATTED_ENUM_NAME'
     *
     * @param str String input
     * @return String formatted for enum fields
     */
    public static String enumName(String str) {
        return str == null ? "" : str.toUpperCase().replace(" ", "_").replace("-", "_");
    }
}
