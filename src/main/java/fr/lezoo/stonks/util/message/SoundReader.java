package fr.lezoo.stonks.util.message;

import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class SoundReader {
    private final Sound sound;
    private final float vol, pitch;

    /**
     * Default sound
     */
    public SoundReader(Sound sound, float vol, float pitch) {
        this.sound = sound;
        this.vol = vol;
        this.pitch = pitch;
    }

    /**
     * Reads a sound from a config file
     *
     * @param config Configuration section containing sound information
     */
    public SoundReader(ConfigurationSection config) {
        Validate.notNull(config, "Config cannot be null");

        String format = config.getString("name");
        Validate.notNull(format, "Could not read sound name");

        sound = Sound.valueOf(format.toUpperCase().replace(" ", "_").replace("-", "_"));
        vol = (float) config.getDouble("vol");
        pitch = (float) config.getDouble("pitch");
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return vol;
    }

    public float getPitch() {
        return pitch;
    }

    public void play(Player player) {
        player.playSound(player.getLocation(), sound, vol, pitch);
    }
}
