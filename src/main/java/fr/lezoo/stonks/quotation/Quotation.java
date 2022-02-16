package fr.lezoo.stonks.quotation;


import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.display.board.Board;
import fr.lezoo.stonks.display.board.QuotationBoardRenderer;
import fr.lezoo.stonks.util.Utils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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
public class Quotation {
    protected final String id, name;
    private final Dividends dividends;

    /**
     * The material that will be exchanged. If this is set to null,
     * that means the quotation is virtual and exchanges money instead
     */
    private final ExchangeType exchangeType;

    /**
     * List of data for every scale. Allows to store just the right
     * amount of data needed so that there aren't 10s timestamps on the yearly scale.
     */
    protected final Map<TimeScale, List<QuotationInfo>> quotationData = new HashMap<>();

    private double previousDemand, currentDemand;

    /**
     * New virtual quotation without dividends
     *
     * @param id                 Internal quotation id
     * @param name               Name of the stock
     * @param firstQuotationData Info containing the quotation initial price
     */
    public Quotation(String id, String name, QuotationInfo firstQuotationData) {
        this(id, name, null, null, firstQuotationData);
    }

    /**
     * New virtual or physical quotations without dividends
     *
     * @param id                 Internal quotation id
     * @param name               Name of the stock
     * @param exchangeType       The material being exchanged, or null if the quotation is virtual
     * @param firstQuotationData Info containing the quotation initial price
     */
    public Quotation(String id, String name, ExchangeType exchangeType, QuotationInfo firstQuotationData) {
        this(id, name, null, exchangeType, firstQuotationData);
    }

    /**
     * New virtual quotation with dividends
     *
     * @param id                 Internal quotation id
     * @param name               Name of the stock
     * @param dividends          Information about quotation dividends
     * @param firstQuotationData Info containing the quotation initial price
     */
    public Quotation(String id, String name, Dividends dividends, QuotationInfo firstQuotationData) {
        this(id, name, dividends, null, firstQuotationData);
    }

    /**
     * Public constructor to create a new Quotation from scratch
     *
     * @param id                 Internal quotation id
     * @param name               Name of the stock
     * @param dividends          Whether or not this quotations gives dividends to investers
     * @param exchangeType       The material being exchanged, or null if the quotation is virtual
     * @param firstQuotationData The only QuotationInfo that exists
     */
    public Quotation(String id, String name, Dividends dividends, ExchangeType exchangeType, QuotationInfo firstQuotationData) {
        this.id = id.toLowerCase().replace("_", "-").replace(" ", "-");
        this.name = name;
        this.dividends = dividends;
        this.exchangeType = exchangeType;
        for (TimeScale disp : TimeScale.values())
            quotationData.put(disp, Arrays.asList(firstQuotationData));
    }

    /**
     * Loads a quotation from a config section
     */
    public Quotation(ConfigurationSection config) {
        this.id = config.getName();
        this.name = config.getString("name");
        this.dividends = config.contains("dividends") ? new Dividends(this, config.getConfigurationSection("dividends")) : null;

        Material material = config.contains("exchange-type.material") ?
                Material.valueOf(config.getString("exchange-type.material").toUpperCase().replace("-", "_").replace(" ", "_")) : null;
        int modelData = config.contains("exchange-type.model-data") ? config.getInt("exchange-type.model-data") : 0;
        Validate.isTrue(material != Material.AIR, "Cannot use AIR as exchange type");
        exchangeType = material == null ? null : new ExchangeType(material, modelData);

        // We load the different data from the yml
        for (TimeScale time : TimeScale.values()) {
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

    public String getCompany() {
        return name;
    }

    public boolean hasDividends() {
        return dividends != null;
    }

    public Dividends getDividends() {
        return dividends;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public boolean isVirtual() {
        return exchangeType == null;
    }

    public List<QuotationInfo> getData(TimeScale disp) {
        return quotationData.get(disp);
    }

    /**
     * Updates the attributes of Quotation regarding on the time given
     *
     * @param time          the time corresponding to the data
     * @param quotationData the data we want to update
     */
    public void setData(TimeScale time, List<QuotationInfo> quotationData) {
        this.quotationData.put(time, quotationData);
    }

    /**
     * This method compares the current price with the info which time
     * stamp matches the most the time stamp given as parameter
     *
     * @param time Difference of time in the past, in millis
     * @return Growth rate compared to some time ago
     */
    public double getEvolution(TimeScale time) {
        List<QuotationInfo> quotationData = this.getData(time);

        /*
         * Last information in the list corresponds to the latest information.
         * First information corresponds to the oldest, which gives us the growth rate.
         *
         * We need a division by zero check?
         */
        double oldest = quotationData.get(0).getPrice();
        double latest = getPrice();

        return Utils.truncate(100 * (latest - oldest) / oldest, 1);
    }

    /**
     * Creates a 5x5 map of the Quotation to the player
     * gives the player all the maps in his inventory
     */
    public Board createQuotationBoard(boolean hasBeenCreated, Location initiallocation, BlockFace
            blockFace, TimeScale time, int BOARD_WIDTH, int BOARD_HEIGHT) {


        //If there is no blocks in the board it destroys itself
        boolean isEmpty = true;


        // We make sure to not change the location given in argument
        Location location = initiallocation.clone();
        // We create the wall to have the board with ItemFrames on it
        //offset otherwise the location where the block is ambiguous
        location.add(0.5, 0.5, 0.5);

        // We get the direction to build horizontally and vertically
        Vector verticalBuildDirection = new Vector(0, 1, 0);
        Vector horizontalBuildDirection = blockFace.getDirection();

        // We need to clone to have deepmemory of it
        Vector horizontalLineReturn = horizontalBuildDirection.clone();
        horizontalLineReturn.multiply(-BOARD_WIDTH);

        Vector itemFrameDirection = Utils.getItemFrameDirection(blockFace);

        // We get the board corresponding to the one we are creating or updating
        Board board = !hasBeenCreated ? new Board(this, BOARD_HEIGHT, BOARD_WIDTH, initiallocation, time, blockFace)
                : Stonks.plugin.boardManager.getBoard(initiallocation, blockFace);

        //Because of offset bugs about where the block spawns etc relative to the location

        //TODO : FIX LE BUG (Problème: offset quand on clique sur les boutons avec le trading book (parfois il y a un offset d'un bloc parfois pas ...bizarre)
        //Idée : On store dans chaque itemframe la loc de celle ci et on regarde juste la position relative au sein de l'itemframe.


        // we fix it by taking the loc in the middle and then getting the loc at the top left corner of it
        Location saveLocation = location.clone();
        saveLocation.setY(Math.ceil(saveLocation.getY()));
        saveLocation.setX(itemFrameDirection.getX() == 1 ? Math.floor(saveLocation.getX()) : Math.ceil(saveLocation.getX()));
        saveLocation.setZ(itemFrameDirection.getZ() == 1 ? Math.floor(saveLocation.getZ()) : Math.ceil(saveLocation.getZ()));
        // If the board has never been created we register it
        if (!hasBeenCreated)
            Stonks.plugin.boardManager.register(board);


        //get the img for the board
        BufferedImage image = board.getImage(time, BOARD_WIDTH, BOARD_HEIGHT);

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

    public void save(FileConfiguration config) {

        // If the quotation is empty we destroy it to not overload memory and avoid errors
        if (quotationData.get(TimeScale.QUARTERHOUR).size() == 0) {
            config.set(id + ".name", null);
            return;
        }

        config.set(id + ".name", name);


        config.set(id + ".exchange-type.material", exchangeType == null ? null : exchangeType.getMaterial().name());
        config.set(id + ".exchange-type.model-data", exchangeType == null ? 0 : exchangeType.getModelData());
        //We save the information of the data
        for (TimeScale time : TimeScale.values()) {
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
    public double getLowest(TimeScale time) {
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
    public double getHighest(TimeScale time) {
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
        List<QuotationInfo> latest = quotationData.get(TimeScale.QUARTERHOUR);
        return latest.get(latest.size() - 1).getPrice();
    }

    /**
     * Changes the quotation price
     */
    public void refreshQuotation() {
        if (getData(TimeScale.QUARTERHOUR).isEmpty())
            return;

        Random random = new Random();
        double rand = random.nextInt(2);
        double change = rand == 0 ? -1 : 1;
        // The offset due to the offer and demand of the stock
        double offset = Stonks.plugin.configManager.offerDemandImpact * Math.atan(previousDemand) * 2 / Math.PI;
        double currentPrice = getPrice();
        //The change between the currentPrice and nextPrice

        //We multiply by sqrt(t) so that volatility doesn't depend on refreshTime

        change = (change + offset) * Stonks.plugin.configManager.volatility * currentPrice / 20 * Math.sqrt((double) (Stonks.plugin.configManager.quotationRefreshTime) / 3600);

        //The amount of data wanted for each timescale fo the quotation
        int datanumber = Stonks.plugin.configManager.quotationDataNumber;
        //We update all the data List
        for (TimeScale time : TimeScale.values()) {
            //We get the list corresponding to the time
            List<QuotationInfo> workingData = new ArrayList<>();
            workingData.addAll(this.getData(time));
            //If the the latest data of workingData is too old we add another one
            if (System.currentTimeMillis() - workingData.get(workingData.size() - 1).getTimeStamp() > time.getTime() / datanumber) {

                workingData.add(new QuotationInfo(System.currentTimeMillis(), currentPrice + change));
                //If the list contains too much data we remove the older ones
                if (workingData.size() > datanumber)
                    workingData.remove(0);
                //We save the changes we made in the attribute
                this.setData(time, workingData);
            }


        }

    }

    public void addDemand(double stockBought) {
        currentDemand += stockBought;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quotation quotation = (Quotation) o;
        return id.equals(quotation.id) && name.equals(quotation.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
