package fr.lezoo.stonks.api.quotation;

import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.image.BufferedImage;

public class QuotationBoardRenderer extends MapRenderer {

    private BufferedImage image;
    private boolean done=false;
    public QuotationBoardRenderer(BufferedImage image) {
        //we resize image to take less RAM
        this.image = MapPalette.resizeImage(image);
    }

    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player){
        //We render the map only if it's hasn't been done
        if(done)
            return;
        mapCanvas.drawImage(0,0,image);
        done=true;
    }


}
