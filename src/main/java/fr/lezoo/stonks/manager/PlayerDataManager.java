package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager implements FileManager {
    private final Map<UUID, PlayerData> map = new HashMap<>();

    public PlayerData get(OfflinePlayer player) {
        return map.get(player.getUniqueId());
    }

    @Override
    public void load() {

        // Load player data of online players
        Bukkit.getOnlinePlayers().forEach(player -> map.put(player.getUniqueId(), new PlayerData(player)));
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

    @Override
    public void save() {

        // Save player data
        for (PlayerData player : map.values()) {
            ConfigFile config = new ConfigFile("/userdata", player.getUniqueId().toString());
            player.saveInConfig(config.getConfig());
            config.save();
        }
    }
}
