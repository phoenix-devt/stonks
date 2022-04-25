package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.manager.ConfigManager;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.TimeScale;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

public class Board {
    private final UUID uuid;
    private final Quotation quotation;
    private final int height, width;
    private final Location location;
    private final TimeScale time;
    private final BlockFace boardFace;
    private final Vector boardWallDirection;
    private final BoardPoint[][] pointArray;

    private static final Material BACKGROUND_MATERIAL = Material.POLISHED_DIORITE;

    private static final NamespacedKey BOARD_ID = new NamespacedKey(Stonks.plugin, "BoardId"),
            BOARD_X = new NamespacedKey(Stonks.plugin, "BoardX"),
            BOARD_Y = new NamespacedKey(Stonks.plugin, "BoardY");

    public Board(Quotation quotation, int width, int height, Location location, TimeScale time, BlockFace boardFace) {
        this.uuid = UUID.randomUUID();
        this.quotation = quotation;
        this.width = width;
        this.height = height;
        this.location = location;
        this.time = time;
        this.boardFace = boardFace;
        this.boardWallDirection = boardFace.getDirection().rotateAroundY(Math.PI / 2);
        this.pointArray = new BoardPoint[width][height];

        initializeBoard(false);
        Stonks.plugin.boardManager.register(this);
    }

    public Board(ConfigurationSection config) {
        uuid = UUID.fromString(config.getName());
        quotation = Stonks.plugin.quotationManager.get(config.getString("quotationid"));
        width = config.getInt("width");
        height = config.getInt("height");
        location = new Location(Bukkit.getWorld(config.getString("world")), config.getInt("x"), config.getInt("y"), config.getInt("z"));
        time = TimeScale.valueOf(config.getString("time").toUpperCase());
        boardFace = BlockFace.valueOf(config.getString("direction"));
        this.boardWallDirection = boardFace.getDirection().rotateAroundY(Math.PI / 2);
        this.pointArray = new BoardPoint[width][height];

        initializeBoard(true);
    }

    private void initializeBoard(boolean checkForItemFrames) {

        // Initialize point array
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                pointArray[x][y] = new BoardPoint(x, y);

        if (checkForItemFrames)
            lookForPreviousItemFrames();

        // Refresh image and item frames
        refreshImage();
        checkItemFrames();
    }

    private void lookForPreviousItemFrames() {
        for (ItemFrame checked : location.getWorld().getEntitiesByClass(ItemFrame.class)) {
            if (!checked.getPersistentDataContainer().has(new NamespacedKey(Stonks.plugin, "BoardId"), PersistentDataType.STRING))
                continue;

            UUID boardId = UUID.fromString(checked.getPersistentDataContainer().get(BOARD_ID, PersistentDataType.STRING));
            if (!boardId.equals(uuid))
                continue;

            int x = checked.getPersistentDataContainer().get(BOARD_X, PersistentDataType.INTEGER);
            int y = checked.getPersistentDataContainer().get(BOARD_Y, PersistentDataType.INTEGER);
            pointArray[x][y].itemFrame = checked;
        }
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

    public BlockFace getBoardFace() {
        return boardFace;
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
        boarddata.set(uuid + ".direction", boardFace.name());
    }

    public void checkItemFrames() {

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                BoardPoint point = pointArray[x][y];
                ItemFrame itemFrame = point.itemFrame;
                if (itemFrame != null && !itemFrame.isDead()) {
                    if (isAir(itemFrame.getItem()))
                        point.fillItemFrame();
                    continue;
                }

                // Create a new one
                point.createItemFrame();
            }
    }

    private boolean isAir(@Nullable ItemStack item) {
        return item == null || item.getType() == Material.AIR;
    }

    public void remove() {

        // Unregistration
        Stonks.plugin.boardManager.removeBoard(uuid);

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                ItemFrame frame = pointArray[x][y].itemFrame;
                if (frame == null)
                    continue;

                // Remove item and THEN frame
                frame.setItem(null);
                frame.remove();
            }
    }

    public void refreshImage() {
        BufferedImage image = getImage();
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                pointArray[x][y].imageSegment = image.getSubimage(128 * x, 128 * (height - y - 1), 128, 128);
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

        int data_taken = Math.min(Quotation.BOARD_DATA_NUMBER, quotationData.size());

        int index = quotationData.size() - data_taken;

        // Look at the lowest val in the time we look backward to set the scale
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
            x = i * BOARD_WIDTH * 0.8 / Quotation.BOARD_DATA_NUMBER;
            y = 0.95 * BOARD_HEIGHT - (0.7 * BOARD_HEIGHT * (quotationData.get(index + i).getPrice() - minVal) / (maxVal - minVal));
            curve.lineTo(x, y);
        }

        g2d.draw(curve);
        return image;
    }

    public class BoardPoint {
        public final int x, y;

        public BufferedImage imageSegment;
        public ItemFrame itemFrame;

        BoardPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Location getLocation() {
            return location.clone().add(0, y, 0).add(boardWallDirection.clone().multiply(x)).add(boardFace.getDirection());
        }

        private void createItemFrame() {
            ItemFrame itemFrame = this.itemFrame = (ItemFrame) location.getWorld().spawnEntity(getLocation(), EntityType.ITEM_FRAME);
            itemFrame.getPersistentDataContainer().set(new NamespacedKey(Stonks.plugin, "BoardId"), PersistentDataType.STRING, uuid.toString());
            itemFrame.getPersistentDataContainer().set(new NamespacedKey(Stonks.plugin, "BoardX"), PersistentDataType.INTEGER, x);
            itemFrame.getPersistentDataContainer().set(new NamespacedKey(Stonks.plugin, "BoardY"), PersistentDataType.INTEGER, y);
            itemFrame.setFacingDirection(boardFace);
            fillItemFrame();
        }

        private void fillItemFrame() {
            Validate.isTrue(itemFrame != null && !itemFrame.isDead(), "Item frame could not be found or is dead");

            // Create the map that will go in the frame
            ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
            MapMeta meta = (MapMeta) mapItem.getItemMeta();
            MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
            mapView.getRenderers().clear();

            // (j = 0 => x = 0) but (i = 0 => i = BOARD_HEIGHT) because of how graphics 2D works
            mapView.addRenderer(new BoardMapRenderer(this));

            mapView.setTrackingPosition(false);
            mapView.setUnlimitedTracking(false);
            meta.setMapView(mapView);
            mapItem.setItemMeta(meta);

            itemFrame.setItem(mapItem);
        }
    }
}



