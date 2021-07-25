package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.Board;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;


public class BoardManager {
    private final Map<UUID, Board> boards = new HashMap<UUID, Board>();

    public void reload() {
        FileConfiguration config = new ConfigFile("boarddata").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Board(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load board info '" + key + "'");
            }
    }

    public void refreshBoards() {
        boards.values().forEach(board->board.refreshBoard());
    }

    public void save() {
        boards.values().forEach(board -> board.saveBoard());
    }

    public void register(Board board) {
        boards.put(board.getUuid(), board);
    }
}
