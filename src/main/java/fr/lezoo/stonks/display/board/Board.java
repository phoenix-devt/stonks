package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.listener.temp.DropItemListener;
import fr.lezoo.stonks.listener.temp.RemoveBoardListener;
import fr.lezoo.stonks.listener.temp.TemporaryListener;
import fr.lezoo.stonks.manager.ConfigManager;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.TimeScale;
import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class Board {
    private final UUID uuid;
    private final Quotation quotation;
    private final int height, width;
    private final Location location;

    /* private final List<>*/


    private final TimeScale time;

    private final BlockFace direction;

    public Board(Quotation quotation, int height, int width, Location location, TimeScale time, BlockFace direction) {
        uuid = UUID.randomUUID();
        this.quotation = quotation;
        this.height = height;
        this.width = width;
        this.location = location;
        this.time = time;
        this.direction = direction;
    }

    public Board(ConfigurationSection config) {
        uuid = UUID.fromString(config.getName());
        quotation = Stonks.plugin.quotationManager.get(config.getString("quotationid"));
        width = config.getInt("width");
        height = config.getInt("height");
        location = new Location(Bukkit.getWorld(config.getString("world")), config.getInt("x"), config.getInt("y"), config.getInt("z"));
        time = TimeScale.valueOf(config.getString("time").toUpperCase());
        direction = BlockFace.valueOf(config.getString("direction"));
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Location getLocation() {
        return location;
    }

    public TimeScale getTime() {
        return time;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Saves the board into boarddata.yml
     */
    public void saveBoard(FileConfiguration boarddata) {
        boarddata.set(uuid.toString() + ".quotationid", quotation.getId());
        boarddata.set(uuid + ".width", width);
        boarddata.set(uuid + ".height", height);
        boarddata.set(uuid + ".x", location.getX());
        boarddata.set(uuid + ".y", location.getY());
        boarddata.set(uuid + ".z", location.getZ());
        boarddata.set(uuid + ".world", location.getWorld().getName());
        boarddata.set(uuid + ".time", time.toString().toLowerCase());
        boarddata.set(uuid + ".direction", direction.name());
    }

    public void destroy() {
        // We unregister the board
        Stonks.plugin.boardManager.removeBoard(getUuid());
        BlockFace blockFace = getDirection();
        // We destroy the entities
        Location newLocation = location.clone();
        // We create the wall to have the board with ItemFrames on it
        double x = blockFace.getDirection().getX() * 0.5;
        double z = blockFace.getDirection().getZ() * 0.5;
        // We want the block placed behind the location if we are looking at it
        if (x == 0) {
            x = -Utils.rotateAroundY(blockFace).getDirection().getX() * 0.5;
        }
        if (z == 0) {
            z = -Utils.rotateAroundY(blockFace).getDirection().getZ() * 0.5;
        }


        newLocation.add(x, 0.5, z);

        // We get the direction to build horizontally and vertically
        Vector verticalBuildDirection = new Vector(0, 1, 0);
        Vector horizontalBuildDirection = direction.getDirection();

        // We need to clone to have deepmemory of it
        Vector horizontalLineReturn = horizontalBuildDirection.clone();
        horizontalLineReturn.multiply(-width);
        Vector itemFrameDirection = Utils.rotateAroundY(direction).getDirection();

        //We register a new Listener to cancel all the DropItemEvent
        TemporaryListener listener = new DropItemListener();


        // We remove all the blocks of the board
        // We remove them all
        for (int i = 0; i < height; i++) {
            // i stands for the line of the board and j the column
            for (int j = 0; j < width; j++) {
                // We remove the block
                newLocation.getBlock().setType(Material.AIR);
                for (Entity entity : newLocation.getWorld().getEntities()) {
                    if (entity instanceof ItemFrame && entity.getLocation().distance(newLocation) < 2) {
                        entity.remove();
                    }
                }
                newLocation.add(horizontalBuildDirection);
            }
            newLocation.add(verticalBuildDirection);
            newLocation.add(horizontalLineReturn);
        }
        //We close the listener now that the destruction is done
        listener.close();
    }

    /**
     * Refreshes the board
     */

    public void refreshBoard() {
        // We use the createQuotationBoard method and say that it has already been created so we dont register it
        quotation.createQuotationBoard(true, null,location, direction, time, width, height);
    }


    public Placeholders getPlaceholders() {
        Placeholders holders = new Placeholders();
        DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;
        holders.register("quotation-id", quotation.getId());
        holders.register("quotation-name", quotation.getName());
        holders.register("current-price", format.format(quotation.getPrice()));
        holders.register("lowest-price", format.format(quotation.getLowest(time)));
        holders.register("highest-price", format.format(quotation.getHighest(time)));
        holders.register("evolution", quotation.getEvolution(time));
        holders.register("time-scale", time.toString().toLowerCase());
        holders.register("quotation-type", quotation.getClass().getSimpleName());
        holders.register("exchange-type", quotation.getExchangeType() == null ? "money" : quotation.getExchangeType().toString().toLowerCase());
        return holders;
    }

    public BufferedImage getImage() {

        ConfigurationSection config = YamlConfiguration.loadConfiguration(ConfigManager.DefaultFile.BOARD.getFile()).getConfigurationSection("description");
        Placeholders holders = getPlaceholders();


        // There is 128 pixel for each map
        int BOARD_HEIGHT = 128 * height;
        int BOARD_WIDTH = 128 * width;

        // If not enough data on quotation data we take care of avoiding IndexOutOfBounds
        BufferedImage image = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        List<QuotationInfo> quotationData = quotation.getData(time);
        //If the quotation is Empty we print an error
        Validate.isTrue(quotationData.size() != 0, "The quotation : " + quotation.getId() + " has no values!!");

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

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(2, 2, BOARD_WIDTH - 4, BOARD_HEIGHT - 4));

        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setColor(new Color(126, 51, 0));
        g2d.draw(new Rectangle2D.Double(0, 0.2 * BOARD_HEIGHT, BOARD_WIDTH * 0.8, 0.8 * BOARD_HEIGHT));
        g2d.draw(new Line2D.Double(0.8 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT, 0.8 * BOARD_WIDTH, 0));
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(null, Font.BOLD, BOARD_HEIGHT * 3 / 128));
        // We want only 2 numbers after the comma
        g2d.drawString(holders.apply(config.getString("time-scale")), (int) (0.03 * BOARD_WIDTH), (int) (0.04 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("quotation-name")), (int) (0.03 * BOARD_WIDTH), (int) (0.08 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("quotation-type")), (int) (0.03 * BOARD_WIDTH), (int) (0.12 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("exchange-type")), (int) (0.03 * BOARD_WIDTH), (int) (0.16 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("current-price")), (int) (0.45 * BOARD_WIDTH), (int) (0.04 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("lowest-price")), (int) (0.45 * BOARD_WIDTH), (int) (0.08 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("highest-price")), (int) (0.45 * BOARD_WIDTH), (int) (0.12 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(config.getString("evolution")), (int) (0.45 * BOARD_WIDTH), (int) (0.16 * BOARD_HEIGHT));

        g2d.setColor(new Color(80, 30, 0));
        // Bouton SELL,SHORT,BUY,SET LEVERAGE
        // 0.82*BOARD_WIDTH to 0.98
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.02 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.19 * BOARD_HEIGHT));
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.25 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT));
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.5 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT));
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.75 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT));
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font(null, Font.BOLD, (int) (BOARD_HEIGHT * 3.5 / 128)));
        g2d.drawString("PARAMETERS", (int) (0.825 * BOARD_WIDTH), (int) (0.1 * BOARD_HEIGHT));
        g2d.setFont(new Font(null, Font.BOLD, BOARD_HEIGHT * 4 / 128));
        g2d.drawString("BUY", (int) (0.835 * BOARD_WIDTH), (int) (0.35 * BOARD_HEIGHT));
        g2d.drawString("SHORT", (int) (0.83 * BOARD_WIDTH), (int) (0.60 * BOARD_HEIGHT));
        g2d.drawString("ORDERS", (int) (0.83 * BOARD_WIDTH), (int) (0.85 * BOARD_HEIGHT));

        g2d.setColor(Color.RED);
        Path2D.Double curve = new Path2D.Double();
        // If price = maxVal y =0.25 IMAGE_SIZE
        // If price = min Val y=0.95*IMAGE_SIZE (BOTTOM)
        double x = 0;
        double y = 0.95 * BOARD_HEIGHT - (0.7 * BOARD_HEIGHT * (quotationData.get(index).getPrice() - minVal) / (maxVal - minVal));
        curve.moveTo(x, y);
        for (int i = 1; i < data_taken; i++) {
            // if data_taken < NUMBER_DATA,the graphics will be on the left of the screen mainly
            x = i * BOARD_WIDTH * 0.8 / Stonks.plugin.configManager.quotationDataNumber;
            y = 0.95 * BOARD_HEIGHT - (0.7 * BOARD_HEIGHT * (quotationData.get(index + i).getPrice() - minVal) / (maxVal - minVal));
            curve.lineTo(x, y);
        }

        g2d.draw(curve);
        return image;
    }
}



