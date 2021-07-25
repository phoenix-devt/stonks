package fr.lezoo.stonks.api.quotation;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.ConfigFile;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.util.Vector;

import java.util.UUID;

public class BoardInfo {
    private final UUID uuid;
    private final Quotation quotation;
    private final int height;
    private final int width;
    private final Vector location;
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

    public Vector getLocation() {
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

    public BoardInfo(Quotation quotation, int height, int width, Vector location, int numberdata, BlockFace direction) {
        uuid=UUID.randomUUID();
        this.quotation = quotation;
        this.height = height;
        this.width = width;
        this.location = location;
        this.numberdata = numberdata;
        this.direction = direction;
    }

    public BoardInfo(ConfigurationSection configSection) {
        uuid =UUID.fromString(configSection.getName());
        quotation= Stonks.plugin.quotationManager.get(configSection.getString("quotationid"));
        width=configSection.getInt("width");
        height=configSection.getInt("height");
        location=new Vector(configSection.getInt("x"),configSection.getInt("y"),configSection.getInt("z"));
        numberdata=configSection.getInt("numberdata");
        direction=BlockFace.valueOf(configSection.getString("direction"));
    }

    /**
     * Saves the board into boarddata.yml
     */
    public void saveBoard() {
        ConfigFile configfile = new ConfigFile("boarddata");
        FileConfiguration fileConfig=configfile.getConfig();
        fileConfig.set(uuid.toString()+".quotationid",quotation.getId());
        fileConfig.set(uuid.toString()+".width",width);
        fileConfig.set(uuid.toString()+".height",height);
        fileConfig.set(uuid.toString()+".x",location.getX());
        fileConfig.set(uuid.toString()+".y",location.getY());
        fileConfig.set(uuid.toString()+".z",location.getZ());
        fileConfig.set(uuid.toString()+".numberdata",numberdata);
        fileConfig.set(uuid.toString()+".direction",direction.name());
        //We pass the modifciations on the RAM into the yml file
        configfile.save();
    }


}
