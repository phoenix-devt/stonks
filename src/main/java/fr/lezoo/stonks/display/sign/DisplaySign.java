package fr.lezoo.stonks.display.sign;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
import fr.lezoo.stonks.util.Position;
import fr.lezoo.stonks.util.Utils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DisplaySign {
    private final Quotation quotation;
    private final Position pos;

    private long lastCheck;

    private static final long CHECK_TIME_OUT = 1000 * 10;

    /**
     * Used when creating a display sign using the special command
     *
     * @param quotation Quotation to take info from
     * @param pos       Position of the display sign
     */
    public DisplaySign(Quotation quotation, Position pos) {
        this.quotation = quotation;
        this.pos = pos;

        update();
    }

    /**
     * Used when loading a display sign from the save file
     */
    public DisplaySign(ConfigurationSection config) {
        quotation = Objects.requireNonNull(Stonks.plugin.quotationManager.get(config.getString("quotation")), "Could not find quotation");
        pos = Position.from(config.getConfigurationSection("position"));

        update();
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public Position getPosition() {
        return pos;
    }

    public void update() {

        // Reduce amount of useless checks
        if (System.currentTimeMillis() < lastCheck + CHECK_TIME_OUT)
            return;

        Location loc = pos.toLocation();
        if (!loc.getBlock().getType().name().contains("SIGN")) {
            lastCheck = System.currentTimeMillis();
            return;
        }

        // Fetch sign format
        List<String> format = Stonks.plugin.configManager.displaySignFormat;

        // Update sign
        Sign sign = (Sign) loc.getBlock().getState();
        for (int j = 0; j < 4; j++)
            sign.setLine(j, applyPlaceholders(quotation, format.get(j)));

        // Save block state update
        sign.update();
    }

    public void save(ConfigurationSection config) {
        UUID randomId = UUID.randomUUID();
        config.set(randomId + ".quotation", quotation.getId());

        config.set(randomId + ".position.world", pos.getWorld().getName());
        config.set(randomId + ".position.x", pos.getX());
        config.set(randomId + ".position.y", pos.getY());
        config.set(randomId + ".position.z", pos.getZ());
    }

    private String applyPlaceholders(Quotation quotation, String input) {
        return input.replace("{name}", quotation.getName())
                .replace("{price}", Stonks.plugin.configManager.stockPriceFormat.format(quotation.getPrice()))
                .replace("{day-evolution}", Utils.formatRate(quotation.getEvolution(TimeScale.DAY)))
                .replace("{week-evolution}", Utils.formatRate(quotation.getEvolution(TimeScale.WEEK)));
    }
}
