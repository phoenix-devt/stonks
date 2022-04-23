package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.manager.BoardMapManager;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class QuotationBoardRenderer extends MapRenderer {

    private final BoardMapInfo mapInfo;
    private int counter = 0;

    public QuotationBoardRenderer(BoardMapInfo mapInfo) {
        this.mapInfo = mapInfo;
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        counter++;
        //We render the map only if it's hasn't been done
        if (counter < Stonks.plugin.configManager.boardRefreshTime * 20)
            return;


        BufferedImage image = MapPalette.resizeImage(Stonks.plugin.boardMapManager.getMapImage(mapInfo));
        mapCanvas.drawImage(0, 0, image);
        counter = 0;
    }


}
