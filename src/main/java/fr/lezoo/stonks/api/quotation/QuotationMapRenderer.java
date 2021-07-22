package fr.lezoo.stonks.api.quotation;


import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Used to render the quotation evolution on a map item
 */
public class QuotationMapRenderer extends MapRenderer {
    private final List<QuotationInfo> quotationData;
    //It will update every TIMEOUT tick the Map
    private static final int TIMEOUT=200;
    //Count the number of ticks
    private int iterations=0;

    public QuotationMapRenderer(List<QuotationInfo> quotationData) {
        this.quotationData=quotationData;
    }


    @Override
    public void render(MapView mapView,MapCanvas mapCanvas, Player player) {
        if (iterations==TIMEOUT) {
            BufferedImage image=this.getQuotationImage();
            //We resize too take less RAM
            image= MapPalette.resizeImage(image);
            //draw image on canvas
            mapCanvas.drawImage(0,0,image);
            //We don't see player on map
            mapView.setTrackingPosition(false);

            iterations=0;
        }
        iterations++;


    }
    public BufferedImage getQuotationImage() {

return null;
    }

}
