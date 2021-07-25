package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.api.ConfigFile;
import fr.lezoo.stonks.api.quotation.BoardInfo;


import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class BoardManager {
    private Map<UUID, BoardInfo> boards = new HashMap<UUID,BoardInfo>();

    /**
     *
     */
    public void reload() {
        ConfigFile configFile = new ConfigFile("boarddata");
        //We use the
        configFile.getConfig().getKeys(false)
                .forEach(uuid->boards.put(UUID.fromString(uuid),new BoardInfo(configFile.getConfig().getConfigurationSection(uuid))));
    }

    public void save() {
        boards.values().forEach(boardInfo -> boardInfo.saveBoard());
    }

    public void register(BoardInfo boardInfo) {

        boards.put(boardInfo.getUuid(),boardInfo);
    }

}
