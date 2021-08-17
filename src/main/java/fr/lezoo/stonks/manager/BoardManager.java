package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.quotation.board.Board;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;

import java.util.*;
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
     *
     * @return
     */
    @Nullable
    public Board getBoard(Location location, BlockFace direction) {
        for (Board board : boards.values()) {
            if (board.getLocation().equals(location) && board.getDirection().equals(direction))
                return board;
        }
        //If the boards doesn't exist
        Stonks.plugin.getLogger().log(Level.WARNING, "You tried to get a board that doesn't exist");
        save();
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
        //We avoid ConcurrentModificationException
        for (UUID key : boards.keySet())
            boards.get(key).refreshBoard();
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