package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.board.Board;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class BoardManager implements FileManager {
    private final Map<UUID, Board> boards = new HashMap<>();

    public Board getBoard(UUID uuid) {
        return boards.get(uuid);
    }

    public void removeBoard(UUID uuid) {
        boards.remove(uuid);
    }


    /**
     * The location and direction of the board is a key for the boards that we can use
     * @return
     */

    public Board getBoard(Location location, BlockFace direction) {
        for (Board board : boards.values()) {
            if (board.getLocation() == location && board.getDirection() == direction)
                return board;
        }
        //If the boards doesn't exist
        Stonks.plugin.getLogger().log(Level.WARNING, "You tried to get a board that doesn't exist");
        return null;
    }


    @Override
    public void load() {
        FileConfiguration config = new ConfigFile("boarddata").getConfig();
        for (String key : config.getKeys(false))
            try {
                register(new Board(config.getConfigurationSection(key)));
            } catch (IllegalArgumentException exception) {
                Stonks.plugin.getLogger().log(Level.WARNING, "Could not load board info '" + key + "'");
            }
    }

    public void refreshBoards() {
        boards.values().forEach(board -> board.refreshBoard());
    }

    @Override
    public void save() {
        ConfigFile configfile = new ConfigFile("boarddata");

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
