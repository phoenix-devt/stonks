package fr.lezoo.stonks.api;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.share.Share;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import java.util.*;

public class PlayerData {
    private final UUID uuid;
    private Player player;

    // Data not saved when logging off
    private double leverage = 1;

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

    public void addShare(Quotation quotation, Share share) {
        if (!shares.containsKey(quotation.getId()))
            shares.put(quotation.getId(), new HashSet<>());

        this.shares.get(quotation.getId()).add(share);
    }

    public double getLeverage() {
        return leverage;
    }

    public void setLeverage(double leverage) {
        Validate.isTrue(leverage > 0, "Leverage must be >0");
        this.leverage = leverage;
    }

    public Set<Share> getShares(Quotation quotation) {
        return shares.getOrDefault(quotation.getId(), new HashSet<>());
    }

    public static PlayerData get(Player player) {
        return Stonks.plugin.playerManager.get(player);
    }
}
