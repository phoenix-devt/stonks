package fr.lezoo.stonks.api.quotation;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Used to render the quotation evolution on a map item
 */
public class QuotationMapRenderer extends MapRenderer {
    private final Quotation quotation;
    private final List<QuotationInfo> quotationData;
    //Number of pixels on the BufferedImage drawn
    private static final double IMAGE_SIZE=512;
    //It will update every TIMEOUT tick the Map
    private final int DATA_NUMBER;
    //Timeout de 30s
    private static final int TIMEOUT = 20;
    //Count the number of ticks
    private int iterations = 0;


    public QuotationMapRenderer(Quotation quotation, int DATA_NUMBER) {
        this.quotationData = quotation.getQuotationData();
        this.quotation=quotation;
        //We take the min of the theoric DATA_NUMBER that we want and the real length size of quotationData to avoid IndexOutOfBounds
        this.DATA_NUMBER = Math.min(DATA_NUMBER,quotationData.size());
    }


    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        /*
        if (iterations >= TIMEOUT) {
            BufferedImage image = this.getQuotationImage();
            //We resize to take less RAM
            image = MapPalette.resizeImage(image);
            //draw image on canvas
            mapCanvas.drawImage(0, 0, image);
            //We don't see player on map
            mapView.setUnlimitedTracking(true);
            mapView.setTrackingPosition(false);
            iterations = 0;
        }
        iterations++;
*/

    }



    public List<QuotationInfo> getQuotationData() {
        return quotationData;
    }






}
