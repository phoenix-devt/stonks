package fr.lezoo.stonks.quotation.map;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.QuotationTimeDisplay;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Used to render the quotation evolution on a map item
 */
public class QuotationMapRenderer extends MapRenderer {
    private final Quotation quotation;
    //Number of pixels on the BufferedImage drawn
    private static final double IMAGE_SIZE = 512;
    private int datataken;
    //Count the number of ticks
    private List<QuotationInfo> quotationData;
    private QuotationTimeDisplay time;
    private boolean done;


    public QuotationMapRenderer(Quotation quotation, QuotationTimeDisplay time) {
        this.quotation = quotation;
        quotationData=quotation.getCorrespondingData(time);
        this.time=time;
        //We take the min of the theoric DATA_NUMBER that we want and the real length size of quotationData to avoid IndexOutOfBounds
        this.datataken = Math.min(quotationData.size(), Stonks.plugin.configManager.quotationDataNumber);
    }

    public BufferedImage getQuotationImage() {
        BufferedImage image =new BufferedImage(128,128,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d= (Graphics2D) image.getGraphics();
        
        List<QuotationInfo> quotationData = quotation.getCorrespondingData(time);
        //If the quotation is Empty we print an error
        Validate.isTrue(quotationData.size()!=0,"The quotation : "+quotation.getId()+" has no values!!");

        int data_taken = Math.min(Stonks.plugin.configManager.quotationDataNumber, quotationData.size());

        int index = quotationData.size() - data_taken;

        // We look at the lowest val in the time we look backward to set the scale
        double minVal = quotationData.get(index).getPrice();
        double maxVal = quotationData.get(index).getPrice();
        for (int i = 1; i < data_taken; i++) {
            if (quotationData.get(index + i).getPrice() > maxVal)
                maxVal = quotationData.get(index + i).getPrice();
            if (quotationData.get(index + i).getPrice() < minVal)
                minVal = quotationData.get(index + i).getPrice();
        }
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(0,0,128,128));


        g2d.setColor(Color.RED);
        Path2D.Double curve = new Path2D.Double();
        // If price = maxVal y =0.05 IMAGE_SIZE
        // If price = min Val y=0.95*IMAGE_SIZE (BOTTOM)
        double x = 5;
        double y = 0.95*128 - (0.9 * 128 * (quotationData.get(index).getPrice() - minVal) / (maxVal - minVal));
        curve.moveTo(x, y);
        for (int i = 1; i < data_taken; i++) {
            // if data_taken < NUMBER_DATA,the graphics will be on the left of the screen mainly
            x = 5+i * 128/0.95  / Stonks.plugin.configManager.quotationDataNumber;
            y = 0.95*128 - (0.9 * 128 * (quotationData.get(index + i).getPrice() - minVal) / (maxVal - minVal));
            curve.lineTo(x, y);
        }
        g2d.draw(curve);
        return image;
    }




    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
            if(!done) {
                BufferedImage image = this.getQuotationImage();
                //We resize to take less RAM
                image = MapPalette.resizeImage(image);
                //draw image on canvas
                mapCanvas.drawImage(0, 0, image);
                //We don't see player on map
                mapView.setUnlimitedTracking(true);
                mapView.setTrackingPosition(false);
                done=true;
            }


    }
}

