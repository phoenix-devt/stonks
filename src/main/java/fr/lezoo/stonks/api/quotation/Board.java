package fr.lezoo.stonks.api.quotation;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapView;
import org.bukkit.util.Vector;

import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.logging.Level;

public class Board {
    private final UUID uuid;
    private final Quotation quotation;
    private final int height;
    private final int width;
    private final Location location;
    private final int numberdata;
    private BlockFace direction;

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

    public int getNumberdata() {
        return numberdata;
    }

    public BlockFace getDirection() {
        return direction;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Board(Quotation quotation, int height, int width, Location location, int numberdata, BlockFace direction) {
        uuid = UUID.randomUUID();
        this.quotation = quotation;
        this.height = height;
        this.width = width;
        this.location = location;
        this.numberdata = numberdata;
        this.direction = direction;
    }

    public Board(ConfigurationSection configSection) {
        uuid = UUID.fromString(configSection.getName());
        quotation = Stonks.plugin.quotationManager.get(configSection.getString("quotationid"));
        width = configSection.getInt("width");
        height = configSection.getInt("height");
        location = new Location(Bukkit.getWorld(configSection.getString("world")), configSection.getInt("x"), configSection.getInt("y"), configSection.getInt("z"));
        numberdata = configSection.getInt("numberdata");
        direction = BlockFace.valueOf(configSection.getString("direction"));
    }

    /**
     * Saves the board into boarddata.yml
     */
    public void saveBoard() {
        ConfigFile configfile = new ConfigFile("boarddata");
        FileConfiguration fileConfig = configfile.getConfig();
        fileConfig.set(uuid.toString() + ".quotationid", quotation.getId());
        fileConfig.set(uuid.toString() + ".width", width);
        fileConfig.set(uuid.toString() + ".height", height);
        fileConfig.set(uuid.toString() + ".x", location.getX());
        fileConfig.set(uuid.toString() + ".y", location.getY());
        fileConfig.set(uuid.toString() + ".z", location.getZ());
        fileConfig.set(uuid.toString() + ".world", location.getWorld().getName());
        fileConfig.set(uuid.toString() + ".numberdata", numberdata);
        fileConfig.set(uuid.toString() + ".direction", direction.name());
        //We pass the modifications on the RAM into the yml file
        configfile.save();
    }


    public void destroy() {
        //We un register the event
        Stonks.plugin.boardManager.removeBoard(uuid);
        //We destroy the entities
        Location newlocation = location.clone();
        //We create the wall to have the board with ItemFrames on it
        newlocation.add(0.5, 0.5, 0.5);
        //We get the direction to build horizontally and vertically
        Vector verticalBuildDirection = new Vector(0, 1, 0);
        Vector horizontalBuildDirection = direction.getDirection();

        //We need to clone to have deepmemory of it
        Vector horizontalLineReturn = horizontalBuildDirection.clone();
        horizontalLineReturn.multiply(-width);
        Vector itemFrameDirection = quotation.getItemFrameDirection(direction);

        //We get to the layer of ITemFrames
        newlocation.add(itemFrameDirection);
        //We remove them all
        for (int i = 0; i < height; i++) {
            //i stands for the line of the board and j the column
            for (int j = 0; j < width; j++) {
                //We remove the frame on the block
                newlocation.getWorld().getNearbyEntities(newlocation, 0.5, 0.5, 0.5, entity -> entity instanceof ItemFrame)
                        .forEach(itemFrame -> itemFrame.remove());
                newlocation.add(horizontalBuildDirection);
            }
            newlocation.add(verticalBuildDirection);
            newlocation.add(horizontalLineReturn);
        }

    }

    /**
     * Refresh the board
     */
    public void refreshBoard() {
        //We use the createQuotationBoard method and say that it has already been created so we dont register it
        quotation.createQuotationBoard(true, location, direction, numberdata, width, height);


    }


}



