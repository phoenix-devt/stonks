package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final Map<UUID, PlayerData> map = new HashMap<>();

    public PlayerData get(OfflinePlayer player) {
        return map.get(player.getUniqueId());
    }

    /**
     * Called when a player logs on the server
     */
    public void setup(Player player) {
        if (map.containsKey(player.getUniqueId()))
            map.get(player.getUniqueId()).updatePlayer(player);
        else
            map.put(player.getUniqueId(), new PlayerData(player));
    }

    public void save() {

        // Save player data
        for (PlayerData player : map.values()) {
            ConfigFile config = new ConfigFile("/userdata", player.getUniqueId().toString());
            player.saveInConfig(config.getConfig());
            config.save();
        }
    }
}
