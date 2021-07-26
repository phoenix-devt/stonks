package fr.lezoo.stonks.api.util;

import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;

import java.time.LocalDateTime;

public class ConfigSchedule {
    private int hour, min;

    public ConfigSchedule(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        this.hour = config.getInt("hour");
        this.min = config.getInt("min");
    }

    public static boolean isBetween(ConfigSchedule from, ConfigSchedule to) {
        LocalDateTime date = LocalDateTime.now();
        int currentHour = date.getHour(), currentMin = date.getMinute();

        if (to.hour == from.hour)
            return from.min < currentMin && currentMin < to.min;

        if (currentHour == from.hour)
            return from.min < currentMin;

        if (currentHour == to.hour)
            return currentMin < to.min;

        // Schema
        // ------------------/======
        // =======/-----------------
        if (to.hour < from.hour)
            return currentHour > from.hour || currentHour < to.hour;

        // Schema
        // -------/==========/------
        return from.hour < currentHour && currentHour < to.hour;
    }
}
