package fr.lezoo.stonks.version;

import fr.lezoo.stonks.Stonks;
import org.bukkit.Sound;

/**
 * Used to support sound names from any version, which change with server version
 * - 1.13+ sound name in name()
 * - Legacy sound name as first parameter
 */
public enum VersionSound {
    ENTITY_ENDERMAN_HURT("ENTITY_ENDERMAN_HURT", "ENTITY_ENDERMEN_HURT"),
    ENTITY_ENDERMAN_DEATH("ENTITY_ENDERMAN_DEATH", "ENTITY_ENDERMEN_DEATH"),
    ENTITY_ENDERMAN_TELEPORT("ENTITY_ENDERMAN_TELEPORT", "ENTITY_ENDERMEN_TELEPORT"),
    ENTITY_FIREWORK_ROCKET_LARGE_BLAST("ENTITY_FIREWORK_ROCKET_LARGE_BLAST", "ENTITY_FIREWORK_LARGE_BLAST"),
    ENTITY_FIREWORK_ROCKET_TWINKLE("ENTITY_FIREWORK_ROCKET_TWINKLE", "ENTITY_FIREWORK_TWINKLE"),
    ENTITY_FIREWORK_ROCKET_BLAST("ENTITY_FIREWORK_ROCKET_BLAST", "ENTITY_FIREWORK_BLAST"),
    ENTITY_ZOMBIE_PIGMAN_ANGRY("ENTITY_ZOMBIFIED_PIGLIN_ANGRY", "ENTITY_ZOMBIE_PIG_ANGRY"),
    BLOCK_NOTE_BLOCK_HAT("BLOCK_NOTE_BLOCK_HAT", "BLOCK_NOTE_HAT"),
    BLOCK_NOTE_BLOCK_PLING("BLOCK_NOTE_BLOCK_PLING", "BLOCK_NOTE_PLING"),
    ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR("ENTITY_ZOMBIE_ATTACK_WOODEN_DOOR", "ENTITY_ZOMBIE_ATTACK_DOOR_WOOD"),
    ENTITY_ENDER_DRAGON_GROWL("ENTITY_ENDER_DRAGON_GROWL", "ENTITY_ENDERDRAGON_GROWL"),
    ENTITY_ENDER_DRAGON_FLAP("ENTITY_ENDER_DRAGON_FLAP", "ENTITY_ENDERDRAGON_FLAP"),

    ;

    private final Sound sound;

    private VersionSound(String v1_16, String legacy) {
        sound = Sound.valueOf(Stonks.plugin.version.isStrictlyHigher(1, 15) ? v1_16
                : Stonks.plugin.version.isStrictlyHigher(1, 12) ? name() : legacy);
    }

    public Sound toSound() {
        return sound;
    }
}
