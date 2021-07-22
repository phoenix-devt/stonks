package fr.lezoo.stonks.api;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.share.Share;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private Player player;

    /**
     * Mapped shares the player bought from a particular quotation
     */
    private final Map<String, Set<Share>> shares = new HashMap<>();

    public PlayerData(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
    }

    public Player getPlayer() {
        return player;
    }

    public void updatePlayer(Player player) {
        this.player = player;
    }

    public static PlayerData get(Player player) {
        return Stonks.plugin.playerManager.get(player);
    }
}
