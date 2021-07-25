package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.BoardInfo;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;


public class BoardManager {
    private final Map<UUID, BoardInfo> boards = new HashMap<UUID, BoardInfo>();

    public void reload() {
        FileConfiguration config = new ConfigFile("boarddata").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new BoardInfo(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load board info '" + key + "'");
            }
    }

    public void save() {
        boards.values().forEach(boardInfo -> boardInfo.saveBoard());
    }

    public void register(BoardInfo boardInfo) {
        boards.put(boardInfo.getUuid(), boardInfo);
    }
}
