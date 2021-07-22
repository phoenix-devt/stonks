package fr.lezoo.stonks.api.quotation;


import org.bukkit.entity.Player;
import org.bukkit.map.MapCanvas;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Used to render the quotation evolution on a map item
 */
public class QuotationMapRenderer extends MapRenderer {
    private final List<QuotationInfo> quotationData;
    //Number of pixels on the BufferedImage drawn
    private static final double IMAGE_SIZE=256;
    //It will update every TIMEOUT tick the Map
    private final int DATA_NUMBER;
    //Timeout de 30s
    private static final int TIMEOUT = 20;
    //Count the number of ticks
    private int iterations = 0;


    public QuotationMapRenderer(List<QuotationInfo> quotationData, int DATA_NUMBER) {
        this.quotationData = quotationData;
        //We take the min of the theoric DATA_NUMBER that we want and the real length size of quotationData to avoid IndexOutOfBounds
        this.DATA_NUMBER = Math.min(DATA_NUMBER,quotationData.size());
    }


    @Override
    public void render(MapView mapView, MapCanvas mapCanvas, Player player) {
        if (iterations >= TIMEOUT) {
            BufferedImage image = this.getQuotationImage();
            //We resize to take less RAM
            image = MapPalette.resizeImage(image);
            //draw image on canvas
            mapCanvas.drawImage(0, 0, image);
            //We don't see player on map
            mapView.setTrackingPosition(false);
            iterations = 0;
        }
        iterations++;


    }
    //TO DO : Fix the timeStamp issues with DATA_NUMBER on the x axis
    public BufferedImage getQuotationImage() {
        //list of what we will display on x and y axis
        //last data at the end of the array
        final long[] timeStampData = new long[DATA_NUMBER];
        final double[] priceData = new double[DATA_NUMBER];
        //We start from the end of the list
        int index =quotationData.size()-1;
        //minimal and maximal values taken by the quotation during the last DATA_NUMBER iterations
        //We initialize their values
        double maxValue=quotationData.get(index).getPrice();
        double minValue=quotationData.get(index).getPrice();;
        for (int i = 0; i < DATA_NUMBER; i++) {
            //We add the data on the arrays
            //timeStamp = time of the data from the newest data created // ex : if data was created 10 s ago timeStampData = 10*1000;
            timeStampData[DATA_NUMBER-i-1]=quotationData.get(index).getTimeStamp()-quotationData.get(index-i).getTimeStamp();
            priceData[DATA_NUMBER-i-1]=quotationData.get(index-i).getPrice();
            //We update the max and min values
            if(maxValue<priceData[DATA_NUMBER-i-1])
                maxValue=priceData[DATA_NUMBER-i-1];
            if(minValue<priceData[DATA_NUMBER-i-1])
                minValue=priceData[DATA_NUMBER-i-1];

        }

        //Blank Image
        BufferedImage image = new BufferedImage((int)IMAGE_SIZE,(int)IMAGE_SIZE,BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d =(Graphics2D)image.getGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fill(new Rectangle2D.Double(0,0,IMAGE_SIZE,IMAGE_SIZE));
        Path2D.Double curb = new Path2D.Double();
        curb.moveTo(0,IMAGE_SIZE-(0.8*IMAGE_SIZE*(priceData[0]-minValue)/(maxValue-minValue)));
        for (int i=1;i<DATA_NUMBER;i++) {
            //x of the point it is at the right if it a newest datapoint
            double x = IMAGE_SIZE*i/DATA_NUMBER;
            //If price data=maxValue y =0.2 IMAGE_SIZE
            //If pricedata=minValue y=IMAGE_SIZE
            double y = IMAGE_SIZE-(0.8*IMAGE_SIZE*(priceData[i]-minValue)/(maxValue-minValue));
            curb.lineTo(x,y);
        }
        g2d.fill(curb);
        return image;

    }

}
