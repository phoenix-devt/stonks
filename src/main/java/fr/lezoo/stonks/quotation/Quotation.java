package fr.lezoo.stonks.quotation;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.display.board.QuotationBoardRenderer;
import fr.lezoo.stonks.display.map.QuotationMapRenderer;
import fr.lezoo.stonks.util.Utils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Level;

/**
 * Place where players can buy and sell shares
 */
public abstract class Quotation {
    protected final String id, companyName, stockName;
    private final Dividends dividends;

    /**
     * List of data for every scale. Allows to store just the right
     * amount of data needed so that there aren't 10s timestamps on the yearly scale.
     */
    protected final Map<QuotationTimeDisplay, List<QuotationInfo>> quotationData = new HashMap<>();

    public Quotation(String id, String companyName, String stockName, Dividends dividends) {
        this.id = id;
        this.companyName = companyName;
        this.stockName = stockName;
        this.dividends = dividends;

        for (QuotationTimeDisplay disp : QuotationTimeDisplay.values())
            quotationData.put(disp, new ArrayList<>());
    }

    /**
     * Public constructor to create a new Quotation from scratch
     *
     * @param id                 Internal quotation id
     * @param companyName        Name of the concerned company
     * @param stockName          Name of the stock
     * @param dividends          Whether or not this quotations gives dividends to investers
     * @param firstQuotationData The only QuotationInfo that exists
     */
    public Quotation(String id, String companyName, String stockName, Dividends dividends, QuotationInfo firstQuotationData) {
        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.companyName = companyName;
        this.stockName = stockName;
        this.dividends = dividends;

        for (QuotationTimeDisplay disp : QuotationTimeDisplay.values())
            quotationData.put(disp, Arrays.asList(firstQuotationData));
    }

    /**
     * Loads a quotation from a config section
     */
    public Quotation(ConfigurationSection config) {
        this.id = config.getName();
        this.companyName = config.getString("company-name");
        this.stockName = config.getString("stock-name");
        this.dividends = config.contains("dividends") ? new Dividends(this, config.getConfigurationSection("dividends")) : null;

        // We load the different data from the yml
        for (QuotationTimeDisplay time : QuotationTimeDisplay.values()) {
            int i = 0;
            List<QuotationInfo> workingQuotation = new ArrayList<>();

            while (config.contains(time.toString().toLowerCase() + "data." + i)) {
                workingQuotation.add(new QuotationInfo(config.getConfigurationSection(time.toString().toLowerCase() + "data." + i)));
                i++;
            }

            // We change the attribute
            this.setData(time, workingQuotation);
        }
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

    public boolean hasDividends() {
        return dividends != null;
    }

    public Dividends getDividends() {
        return dividends;
    }

    public List<QuotationInfo> getData(QuotationTimeDisplay disp) {
        return quotationData.get(disp);
    }

    /**
     * Updates the attributes of Quotation regarding on the time given
     *
     * @param time          the time corresponding to the data
     * @param quotationData the data we want to update
     */
    public void setData(QuotationTimeDisplay time, List<QuotationInfo> quotationData) {
        this.quotationData.put(time, quotationData);
    }

    /**
     * This method compares the current price with the info which time
     * stamp matches the most the time stamp given as parameter
     *
     * @param time Difference of time in the past, in millis
     * @return Growth rate compared to some time ago
     */
    public double getEvolution(QuotationTimeDisplay time) {
        List<QuotationInfo> quotationData = this.getData(time);

        /*
         * Last information in the list corresponds to the latest information.
         * First information corresponds to the oldest, which gives us the growth rate.
         *
         * We need a division by zero check?
         */
        double oldest = quotationData.get(0).getPrice();
        double latest = quotationData.get(quotationData.size() - 1).getPrice();

        return Utils.truncate(100 * (latest - oldest) / oldest, 1);
    }

    /**
     * Creates a 5x5 map of the Quotation to the player
     * gives the player all the maps in his inventory
     */
    public Board createQuotationBoard(boolean hasBeenCreated, Location initiallocation, BlockFace
            blockFace, QuotationTimeDisplay time, int BOARD_WIDTH, int BOARD_HEIGHT) {

        // We get the board corresponding to the one we are creating or updating
        Board board = !hasBeenCreated ? new Board(this, BOARD_HEIGHT, BOARD_WIDTH, initiallocation, time, blockFace)
                : Stonks.plugin.boardManager.getBoard(initiallocation, blockFace);

        //If there is no blocks in the board it destroys itself
        boolean isEmpty = true;

        // If the board has never been created we register it
        if (!hasBeenCreated)
            Stonks.plugin.boardManager.register(board);

        BufferedImage image = board.getImage(time, BOARD_WIDTH, BOARD_HEIGHT);

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
                if (!location.getBlock().isPassable()) {
                    isEmpty = false;
                    location.add(itemFrameDirection);
                    ItemFrame itemFrame = null;
                    for (Entity entity : location.getChunk().getEntities()) {
                        if (entity.getLocation().distance(location) <= 1 && entity instanceof ItemFrame)
                            itemFrame = (ItemFrame) entity;


                    }
                    //If no item frames have been found we create one
                    if (itemFrame == null)
                        itemFrame = (ItemFrame) location.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);

                    //We store the uuid of the board into the entity
                    PersistentDataContainer container = itemFrame.getPersistentDataContainer();
                    container.set(new NamespacedKey(Stonks.plugin, "boarduuid"), PersistentDataType.STRING, board.getUuid().toString());

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

                    itemFrame.setItem(mapItem);
                    location.subtract(itemFrameDirection);

                }

                location.add(horizontalBuildDirection);
            }

            location.add(verticalBuildDirection);
            location.add(horizontalLineReturn);
        }
        if (isEmpty)
            board.destroy();

        return board;
    }

    public static final String MAP_ITEM_TAG_PATH = "StonksQuotationMap";

    /**
     * This map is never updated you have to get another one to get new informations
     *
     * @param time the time the map looks backwards
     * @return A map where we can see the quotation
     */
    public ItemStack createQuotationMap(QuotationTimeDisplay time) {
        ItemStack mapItem = new ItemStack(Material.FILLED_MAP, 1);

        // We cast the ItemMeta into MapMeta
        MapMeta meta = (MapMeta) mapItem.getItemMeta();
        meta.setDisplayName(ChatColor.RED + Stonks.plugin.configManager.quotationMapName + " : " + companyName);
        //Description of the map
        meta.setLore(Arrays.asList(Stonks.plugin.configManager.companyNameText + " : " + companyName,
                Stonks.plugin.configManager.stockNameText + " : " + stockName,
                Stonks.plugin.configManager.currentPriceText + " : " + this.getPrice(),
                Stonks.plugin.configManager.lowestPriceText + " : " + this.getLowest(time),
                Stonks.plugin.configManager.highestPriceText + " : " + this.getHighest(time),
                Stonks.plugin.configManager.evolutionText + " : " + this.getEvolution(time),
                Stonks.plugin.configManager.timeVisualizedText + " : " + time.toString(),
                Stonks.plugin.configManager.quotationTypeText + " : "));


        // Creates a mapview to later change its Renderer and load img
        MapView mapView = Bukkit.createMap(Bukkit.getWorld("world"));
        mapView.getRenderers().clear();
        mapView.addRenderer(new QuotationMapRenderer(this, time));
        meta.setMapView(mapView);
        mapItem.setItemMeta(meta);
        return mapItem;
    }


    public void save(FileConfiguration config) {

        // If the quotation is empty we destroy it to not overload memory and avoid errors
        if (quotationData.get(QuotationTimeDisplay.QUARTERHOUR).size() == 0) {
            config.set(id + ".company-name", null);
            config.set(id + ".stock-name", null);
            return;
        }

        config.set(id + ".company-name", companyName);
        config.set(id + ".stock-name", stockName);
        //We save the information of the data
        for (QuotationTimeDisplay time : QuotationTimeDisplay.values()) {
            List<QuotationInfo> quotationData = this.getData(time);
            //We load the data needed
            for (int i = 0; i < quotationData.size(); i++) {
                config.set(id + "." + time.toString().toLowerCase() + "data." + i + ".price", quotationData.get(i).getPrice());
                config.set(id + "." + time.toString().toLowerCase() + "data." + i + ".timestamp", quotationData.get(i).getTimeStamp());
            }
        }
    }

    /**
     * @param time The time we want to look back for the quotation
     * @return Lowest price for the given time
     */
    public double getLowest(QuotationTimeDisplay time) {
        List<QuotationInfo> quotationData = this.getData(time);
        if (quotationData.size() == 0)
            Stonks.plugin.getLogger().log(Level.WARNING, "Can't get lowest value of quotation '" + id + "' as data is empty");

        double min = quotationData.get(0).getPrice();
        for (QuotationInfo quotationInfo : quotationData)
            if (quotationInfo.getPrice() < min)
                min = quotationInfo.getPrice();
        return min;
    }

    /**
     * @param time The time we want to look back for the quotation
     * @return Highest price for the given time
     */
    public double getHighest(QuotationTimeDisplay time) {
        List<QuotationInfo> quotationData = this.getData(time);
        if (quotationData.size() == 0)
            Stonks.plugin.getLogger().log(Level.WARNING, "Can't get highest value of quotation '" + id + "' as data is empty");

        double max = quotationData.get(0).getPrice();
        for (QuotationInfo quotationInfo : quotationData)
            if (quotationInfo.getPrice() > max)
                max = quotationInfo.getPrice();
        return max;
    }

    /**
     * @return Current quotation price
     */
    public double getPrice() {
        List<QuotationInfo> latest = quotationData.get(QuotationTimeDisplay.QUARTERHOUR);
        return latest.get(latest.size() - 1).getPrice();
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
