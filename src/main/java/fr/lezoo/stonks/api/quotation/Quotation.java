package fr.lezoo.stonks.api.quotation;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.NBTItem;
import fr.lezoo.stonks.api.quotation.board.Board;
import fr.lezoo.stonks.api.quotation.board.QuotationBoardRenderer;
import fr.lezoo.stonks.api.util.Utils;
import fr.lezoo.stonks.version.ItemTag;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Place where players can buy and sell shares
 */
public abstract class Quotation {
    protected final String id, companyName, stockName;
    protected final List<QuotationInfo> quotationData;
    private final Dividends dividends;

    /**
     * Current price, not final because it is updated every so often
     */
    private double price;


    /**
     * Public constructor to create and register a quotation
     *
     * @param id            Internal quotation id
     * @param companyName   Name of the concerned company
     * @param stockName     Name of the stock
     * @param dividends     Whether or not this quotations gives dividends to investers
     * @param quotationData Stock data so far
     */
    public Quotation(String id, String companyName, String stockName, Dividends dividends, List<QuotationInfo> quotationData) {
        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.companyName = companyName;
        this.stockName = stockName;
        this.dividends = dividends;
        this.quotationData = quotationData;
    }

    public Quotation(String id, String companyName, String stockName, Dividends dividends) {
        this(id, companyName, stockName, dividends, new ArrayList<>());
    }

    /**
     * Loads a quotation from a config section
     */
    public Quotation(ConfigurationSection config) {
        this.id = config.getName();
        this.companyName = config.getString("company-name");
        this.stockName = config.getString("stock-name");
        this.dividends = config.contains("dividends") ? new Dividends(this, config.getConfigurationSection("dividends")) : null;
        this.quotationData = new ArrayList<>();

        // TODO load quotation data
    }


    public String getId() {
        return id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getStockName() {
        return stockName;
    }

    public double getPrice() {
        return price;
    }

    public boolean hasDividends() {
        return dividends != null;
    }

    public Dividends getDividends() {
        return dividends;
    }

    public List<QuotationInfo> getQuotationData() {
        return quotationData;
    }

    /**
     * @param timeOut The delay in the past in millis on which the lowest value
     *                is calculated. For instance, if set to 1000 * 60 * 60 * 24,
     *                lowest value will be calculated over the last day.
     *                <p>
     *                For instance, QuotationInfo.WEEK_TIME_OUT or MONTH_TIME_OUT
     * @return Lowest value for the last X millis
     */
    public double getLowest(long timeOut) {
        double lowest = Double.MAX_VALUE;

        // Iterate through the list revert
        int k = quotationData.size() - 1;
        while (k >= 0) {
            QuotationInfo data = quotationData.get(k);
            if (System.currentTimeMillis() - data.getTimeStamp() > timeOut)
                break;

            if (data.getPrice() < lowest)
                lowest = data.getPrice();
            k--;
        }

        return lowest;
    }

    /**
     * @param timeOut The delay in the past in millis on which the highest value
     *                is calculated. For instance, if set to 1000 * 60 * 60 * 24,
     *                highest value will be calculated over the last day.
     *                <p>
     *                For instance, QuotationInfo.WEEK_TIME_OUT or MONTH_TIME_OUT
     * @return Highest value for the last X millis
     */
    public double getHighest(long timeOut) {
        double highest = Double.MIN_VALUE;

        // Iterate through the list revert
        int k = quotationData.size() - 1;
        while (k >= 0) {
            QuotationInfo data = quotationData.get(k);
            if (System.currentTimeMillis() - data.getTimeStamp() > timeOut)
                break;

            if (data.getPrice() > highest)
                highest = data.getPrice();
            k--;
        }

        return highest;
    }

    /**
     * This method compares the current price with the info which time
     * stamp matches the most the time stamp given as parameter
     * <p>
     * TODO use linear interpolation instead
     *
     * @param delta Difference of time in the past, in millis
     * @return Growth rate compared to some time ago
     */
    public double getEvolution(long delta) {
        QuotationInfo info = getClosestInfo(System.currentTimeMillis() - delta);

        // Check if no division by zero?
        double growthRate = 100 * Math.abs((getPrice() - info.getPrice()) / info.getPrice());

        return Utils.truncate(growthRate, 1);
    }

    /**
     * @return Quotation info with closest time
     * stamp to the one given as parameter
     */
    private QuotationInfo getClosestInfo(long timeStamp) {
        Validate.isTrue(!quotationData.isEmpty(), "Quotation data is empty");

        QuotationInfo closest = null;
        long delta = Long.MAX_VALUE;

        for (QuotationInfo info : quotationData)
            if (Math.abs(info.getTimeStamp() - timeStamp) < delta) {
                delta = Math.abs(info.getTimeStamp() - timeStamp);
                closest = info;
            }

        return closest;
    }

    /**
     * Creates a 5x5 map of the Quotation to the player
     * gives the player all the maps in his inventory
     */
    public Board createQuotationBoard(boolean hasBeenCreated, Location initiallocation, BlockFace blockFace, int NUMBER_DATA, int BOARD_WIDTH, int BOARD_HEIGHT) {

        // We get the board corresponding to the one we are creating or updating
        Board board = hasBeenCreated ? new Board(this, BOARD_HEIGHT, BOARD_WIDTH, initiallocation, NUMBER_DATA, blockFace)
                : Stonks.plugin.boardManager.getBoard(initiallocation, blockFace);

        // If the board has never been created we register it
        if (!hasBeenCreated)
            Stonks.plugin.boardManager.register(board);

        BufferedImage image = board.getImage(NUMBER_DATA, BOARD_WIDTH, BOARD_HEIGHT);

        // We make sure to not chang the location given in argument
        Location location = initiallocation.clone();
        // We create the wall to have the board with ItemFrames on it
        location.add(0.5, 0.5, 0.5);
        // We get the direction to build horizontally and vertically
        Vector verticalBuildDirection = new Vector(0, 1, 0);
        Vector horizontalBuildDirection = blockFace.getDirection();

        // We need to clone to have deepmemory of it
        Vector horizontalLineReturn = horizontalBuildDirection.clone();
        horizontalLineReturn.multiply(-BOARD_WIDTH);

        Vector itemFrameDirection = Utils.getItemFrameDirection(blockFace);

        for (int i = 0; i < BOARD_HEIGHT; i++) {
            // i stands for the line of the board and j the column

            for (int j = 0; j < BOARD_WIDTH; j++) {

                if (!hasBeenCreated)
                    location.getBlock().setType(Material.DARK_OAK_WOOD);

                // We check if there is a block to build the frames on
                if (location.getBlock().getType().isBlock()) {

                    //  We catch the IllegalArgumentException of spawnEntity method
                    try {
                        location.add(itemFrameDirection);
                        location.getWorld().getNearbyEntities(location, 0.5, 0.5, 0.5, entity -> entity instanceof ItemFrame).forEach(entity -> entity.remove());

                        // If there is a problem at onz point we stop entirely the creation of the board
                        ItemFrame itemFrame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);

                        // We create the map that will go in the itemframe
                        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);
                        MapMeta meta = (MapMeta) mapItem.getItemMeta();
                        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
                        mapView.getRenderers().clear();

                        // j=0 ->x=0 but i=0 ->i =BOARD_HEIGHT because of how graphics 2D works
                        mapView.addRenderer(new QuotationBoardRenderer(image.getSubimage(128 * j, 128 * (BOARD_HEIGHT - i - 1), 128, 128)));
                        mapView.setTrackingPosition(false);
                        mapView.setUnlimitedTracking(false);
                        meta.setMapView(mapView);
                        mapItem.setItemMeta(meta);

                        // We store the UUID of the board within each ItemFrame of the board
                        NBTItem nbtItem = NBTItem.get(mapItem);
                        ItemTag itemTag = new ItemTag("boarduuid", board.getUuid().toString());
                        nbtItem.addTag(itemTag);
                        mapItem = nbtItem.toItem();
                        itemFrame.setItem(mapItem);
                        location.subtract(itemFrameDirection);
                    } catch (IllegalArgumentException exception) {
                        board.destroy();
                        exception.printStackTrace();
                        return null;
                    }
                }

                location.add(horizontalBuildDirection);
            }

            location.add(verticalBuildDirection);
            location.add(horizontalLineReturn);
        }

        return board;
    }

    public static final String MAP_ITEM_TAG_PATH = "StonksQuotationMap";

    /**
     * @param NUMBER_DATA number of points taken for the graphic
     * @return A map where we can see the quotation
     */
    public ItemStack createQuotationMap(int NUMBER_DATA) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);

        // We cast the ItemMeta into MapMeta
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + "StockPaper of :" + companyName);
        meta.setLore(Arrays.asList(ChatColor.BLUE + "Company name : " + companyName, ChatColor.GREEN + "Stock name : " + stockName));

        // Creates a mapview to later change its Renderer and load img
        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(this, NUMBER_DATA));
        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);

        NBTItem nbtItem = NBTItem.get(mapItem);
        nbtItem.addTag(new ItemTag(MAP_ITEM_TAG_PATH, true));
        return nbtItem.toItem();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quotation quotation = (Quotation) o;
        return id.equals(quotation.id) && companyName.equals(quotation.companyName) && stockName.equals(quotation.stockName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyName, stockName);
    }
}
