package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.util.ConfigFile;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class BoardManager implements FileManager {
    private final Map<UUID, Board> boards = new HashMap<>();

    @Nullable
    public Board getBoard(UUID uuid) {
        return boards.get(uuid);
    }

    public void removeBoard(UUID uuid) {
        boards.remove(uuid);
    }

    public Collection<Board> getBoards() {
        return boards.values();
    }

    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("board-data").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Board(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load board info '" + key + "'");
            }
    }

    public void refreshBoards() {
        for (Board board : boards.values()) {
            board.refreshImage();
            board.checkItemFrames();
        }
    }

    @Override
    public void save() {
        ConfigFile configfile = new ConfigFile("board-data");

        // Remove older (!!)
        configfile.getConfig().getKeys(false).forEach(key -> configfile.getConfig().set(key, null));

        // Save newest
        boards.values().forEach(board -> board.saveBoard(configfile.getConfig()));

        // Last, save
        configfile.save();
    }

    public void register(Board board) {
        boards.put(board.getUuid(), board);
    }
}
