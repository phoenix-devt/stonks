package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.manager.ConfigManager;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.TimeScale;
import fr.lezoo.stonks.util.Utils;
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
import java.util.logging.Level;

public class Board {
    private final UUID uuid;
    private final Stock stock;
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

    public Board(Stock stock, int width, int height, Location location, TimeScale time, BlockFace boardFace) {
        this.uuid = UUID.randomUUID();
        this.stock = stock;
        this.width = width;
        this.height = height;
        this.location = location;
        this.time = time;
        this.boardFace = boardFace;
        this.boardWallDirection = boardFace.getDirection().rotateAroundY(Math.PI / 2);
        this.pointArray = new BoardPoint[width][height];

        initializeBoard(false);
    }

    public Board(ConfigurationSection config) {
        uuid = UUID.fromString(config.getName());
        stock = Stonks.plugin.stockManager.get(config.getString("stock-id"));
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
        checkBackground();
        checkItemFrames();
    }

    /**
     * Have the board hook onto any previously existing item frames
     * when reloading or restarting the server. If Stonks doesn't
     * look for item frames the ones previously placed down drop on
     * the ground which makes up a mess
     */
    private void lookForPreviousItemFrames() {
        for (ItemFrame checked : location.getWorld().getEntitiesByClass(ItemFrame.class)) {
            if (!checked.getPersistentDataContainer().has(new NamespacedKey(Stonks.plugin, "BoardId"), PersistentDataType.STRING))
                continue;

            UUID boardId = UUID.fromString(checked.getPersistentDataContainer().get(BOARD_ID, PersistentDataType.STRING));
            if (!boardId.equals(uuid))
                continue;

            // Cache reference to the item frame
            int x = checked.getPersistentDataContainer().get(BOARD_X, PersistentDataType.INTEGER);
            int y = checked.getPersistentDataContainer().get(BOARD_Y, PersistentDataType.INTEGER);
            BoardPoint point = pointArray[x][y];
            point.itemFrame = checked;

            // Update item in frame
            checked.setItem(null);
            point.fillItemFrame();
        }
    }

    public Stock getStock() {
        return stock;
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
     * Saves the board into board-data.yml
     */
    public void saveBoard(FileConfiguration config) {
        config.set(uuid.toString() + ".stock-id", stock.getId());
        config.set(uuid + ".width", width);
        config.set(uuid + ".height", height);
        config.set(uuid + ".x", location.getX());
        config.set(uuid + ".y", location.getY());
        config.set(uuid + ".z", location.getZ());
        config.set(uuid + ".world", location.getWorld().getName());
        config.set(uuid + ".time", time.toString().toLowerCase());
        config.set(uuid + ".direction", boardFace.name());
    }

    /**
     * Makes sure there are blocks behind the display board. IF there aren't
     * any at some board point it places one block behind it
     */
    public void checkBackground() {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                BoardPoint point = pointArray[x][y];
                Location location = point.getLocation().clone().subtract(boardFace.getDirection());
                if (location.getBlock().getType().equals(Material.AIR))
                    location.getBlock().setType(BACKGROUND_MATERIAL);
            }
    }

    /**
     * Checks for item frames, fills them if they don't have any item.
     * Also creates them if they are dead/there are none
     */
    public void checkItemFrames() {

        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++) {
                BoardPoint point = pointArray[x][y];
                ItemFrame itemFrame = point.itemFrame;
                if (point.getLocation().getBlock().isPassable()
                        &&!point.getLocation().clone().subtract(boardFace.getDirection()).getBlock().getType().equals(Material.AIR)) {
                    if (itemFrame != null && !itemFrame.isDead()) {
                        if (isAir(itemFrame.getItem()))
                            point.fillItemFrame();
                        continue;
                    }

                    // Create a new one
                    point.createItemFrame();
                }
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
        holders.register("stock-id", stock.getId());
        holders.register("stock-name", stock.getName());
        holders.register("current-price", format.format(stock.getPrice()));
        holders.register("lowest-price", format.format(stock.getLowest(time)));
        holders.register("highest-price", format.format(stock.getHighest(time)));
        holders.register("evolution", stock.getEvolution(time));
        holders.register("time-scale", Utils.caseOnWords(time.toString().toLowerCase()));
        holders.register("stock-type", Utils.caseOnWords(stock.getClass().getSimpleName()));
        holders.register("exchange-type", Utils.caseOnWords(stock.getExchangeType() == null ? "Money" : stock.getExchangeType().toString()));
        return holders;
    }

    public BufferedImage getImage() {
        FileConfiguration config = YamlConfiguration.loadConfiguration(ConfigManager.DefaultFile.BOARD.getFile());
        ConfigurationSection description = config.getConfigurationSection("description");
        ConfigurationSection buttons = config.getConfigurationSection("buttons");
        if (buttons == null) {
            buttons = config.createSection("buttons");
        }
        Placeholders holders = getPlaceholders();


        // There is 128 pixel for each map
        int BOARD_HEIGHT = 128 * height;
        int BOARD_WIDTH = 128 * width;

        // If not enough data on stock data we take care of avoiding IndexOutOfBounds
        BufferedImage image = new BufferedImage(BOARD_WIDTH, BOARD_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        List<StockInfo> stockData = stock.getData(time);
        //If the stock is Empty we print an error
        Validate.isTrue(stockData.size() != 0, "The stock : " + stock.getId() + " has no values!!");

        int data_taken = Math.min(Stock.BOARD_DATA_NUMBER, stockData.size());

        int index = stockData.size() - data_taken;

        // Look at the lowest val in the time we look backward to set the scale
        double minVal = stockData.get(index).getPrice();
        double maxVal = stockData.get(index).getPrice();
        for (int i = 1; i < data_taken; i++) {
            if (stockData.get(index + i).getPrice() > maxVal)
                maxVal = stockData.get(index + i).getPrice();
            if (stockData.get(index + i).getPrice() < minVal)
                minVal = stockData.get(index + i).getPrice();
        }

        // White background
        g2d.setColor(Color.WHITE);
        g2d.fill(new Rectangle2D.Double(2, 2, BOARD_WIDTH - 4, BOARD_HEIGHT - 4));

        g2d.setStroke(new BasicStroke(3.0f));
        g2d.setColor(new Color(126, 51, 0));
        g2d.draw(new Line2D.Double(0, 0.8 * BOARD_HEIGHT, 0.8 * BOARD_WIDTH, 0.8 * BOARD_HEIGHT));
        g2d.draw(new Line2D.Double(0.8 * BOARD_WIDTH, BOARD_HEIGHT, 0.8 * BOARD_WIDTH, 0));
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font(
                description.getString("font.name", null),
                description.contains("font.bold") && !description.getBoolean("font.bold") ? Font.PLAIN : Font.BOLD, (int) (
                        description.getDouble("font.size",1) * BOARD_HEIGHT * 5 / 128.)));
        // We want only 2 numbers after the command
        g2d.drawString(holders.apply(description.getString("time-scale")), (int) (0.03 * BOARD_WIDTH), (int) (0.845 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("stock-name")), (int) (0.03 * BOARD_WIDTH), (int) (0.89 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("stock-type")), (int) (0.03 * BOARD_WIDTH), (int) (0.935 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("exchange-type")), (int) (0.03 * BOARD_WIDTH), (int) (0.98 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("current-price")), (int) (0.45 * BOARD_WIDTH), (int) (0.845 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("lowest-price")), (int) (0.45 * BOARD_WIDTH), (int) (0.89 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("highest-price")), (int) (0.45 * BOARD_WIDTH), (int) (0.935 * BOARD_HEIGHT));
        g2d.drawString(holders.apply(description.getString("evolution")), (int) (0.45 * BOARD_WIDTH), (int) (0.98 * BOARD_HEIGHT));

        g2d.setColor(new Color(80, 30, 0));
        // Bouton SELL,SHORT,BUY,SET LEVERAGE
        // 0.82*BOARD_WIDTH to 0.98
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.02 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.19 * BOARD_HEIGHT));
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.25 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT));
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.5 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT));
        g2d.draw(new Rectangle2D.Double(0.82 * BOARD_WIDTH, 0.75 * BOARD_HEIGHT, 0.16 * BOARD_WIDTH, 0.2 * BOARD_HEIGHT));
        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font(
                buttons.getString("font.name", null),
                buttons.contains("font.bold") && !buttons.getBoolean("font.bold") ? Font.PLAIN : Font.BOLD, (int) (
                description.getDouble("font.size",1) * BOARD_WIDTH * 4 / 128)));
        g2d.drawString(buttons.getString("params", "PARAMS"), (int) (0.825 * BOARD_WIDTH), (int) (0.1 * BOARD_HEIGHT));
        g2d.drawString(buttons.getString("buy", "BUY"), (int) (0.835 * BOARD_WIDTH), (int) (0.35 * BOARD_HEIGHT));
        g2d.drawString(buttons.getString("short", "SHORT"), (int) (0.83 * BOARD_WIDTH), (int) (0.60 * BOARD_HEIGHT));
        g2d.drawString(buttons.getString("orders", "ORDERS"), (int) (0.83 * BOARD_WIDTH), (int) (0.85 * BOARD_HEIGHT));

        g2d.setColor(Color.RED);
        Path2D.Double curve = new Path2D.Double();
        // If price = maxVal y =0.05 IMAGE_SIZE
        // If price = min Val y=0.75*IMAGE_SIZE (BOTTOM)
        double x = 0;
        double y = 0.75 * BOARD_HEIGHT - (0.7 * BOARD_HEIGHT * (stockData.get(index).getPrice() - minVal) / (maxVal - minVal));
        curve.moveTo(x, y);
        for (int i = 1; i < data_taken; i++) {
            // if data_taken < NUMBER_DATA,the graphics will be on the left of the screen mainly
            x = i * BOARD_WIDTH * 0.8 / Stock.BOARD_DATA_NUMBER;
            y = 0.75 * BOARD_HEIGHT - (0.7 * BOARD_HEIGHT * (stockData.get(index + i).getPrice() - minVal) / (maxVal - minVal));
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
            MapView mapView = Bukkit.createMap(itemFrame.getWorld());
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



