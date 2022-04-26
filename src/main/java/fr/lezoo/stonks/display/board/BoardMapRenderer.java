package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

public class BoardMapRenderer extends MapRenderer {
    private final Board.BoardPoint point;

    /**
     * The render method is NOT called every tick since
     * it is placed inside of an item frame. It does update
     * more frequently when placed in the player's inventory
     * <p>
     * https://www.spigotmc.org/threads/maps-in-item-frames-wont-update-every-tick-struggling-to-make-video-player.514621/
     */
    private long lastUpdate;

    public BoardMapRenderer(Board.BoardPoint point) {
        this.point = point;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (System.currentTimeMillis() - lastUpdate < Stonks.plugin.configManager.boardRefreshTime * 1000)
            return;

        lastUpdate = System.currentTimeMillis();
        mapCanvas.drawImage(0, 0, MapPalette.resizeImage(point.imageSegment));
    }
}
