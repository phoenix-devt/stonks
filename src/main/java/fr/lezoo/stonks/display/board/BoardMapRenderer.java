package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class BoardMapRenderer extends MapRenderer {
    private final Board.BoardPoint point;

    private int counter = Integer.MAX_VALUE - 1;

    public BoardMapRenderer(Board.BoardPoint point) {
        this.point = point;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        counter++;
        // We render the map only if it's hasn't been done in a while
        if (counter < Stonks.plugin.configManager.boardRefreshTime * 20)
            return;

        BufferedImage image = MapPalette.resizeImage(point.imageSegment);
        mapCanvas.drawImage(0, 0, image);
        counter = 0;
    }
}
