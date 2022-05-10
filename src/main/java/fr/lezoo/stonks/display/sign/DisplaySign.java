package fr.lezoo.stonks.display.sign;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.TimeScale;
import fr.lezoo.stonks.stock.handler.RealStockHandler;
import fr.lezoo.stonks.util.Position;
import fr.lezoo.stonks.util.Utils;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class DisplaySign {
    private final Stock stock;
    private final Position pos;

    private long lastCheck;

    private static final long CHECK_TIME_OUT = 1000 * 10;

    /**
     * Used when creating a display sign using the special command
     *
     * @param stock Stock to take info from
     * @param pos       Position of the display sign
     */
    public DisplaySign(Stock stock, Position pos) {
        this.stock = stock;
        this.pos = pos;

        update();
    }

    /**
     * Used when loading a display sign from the save file
     */
    public DisplaySign(ConfigurationSection config) {
        stock = Objects.requireNonNull(Stonks.plugin.stockManager.get(config.getString("stock")), "Could not find stock");
        pos = Position.from(config.getConfigurationSection("position"));

        update();
    }

    public Stock getStock() {
        return stock;
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
            sign.setLine(j, applyPlaceholders(stock, format.get(j)));

        // Save block state update
        sign.update();
    }

    public void save(ConfigurationSection config) {
        UUID randomId = UUID.randomUUID();
        config.set(randomId + ".stock", stock.getId());

        config.set(randomId + ".position.world", pos.getWorld().getName());
        config.set(randomId + ".position.x", pos.getX());
        config.set(randomId + ".position.y", pos.getY());
        config.set(randomId + ".position.z", pos.getZ());
    }

    private String applyPlaceholders(Stock stock, String input) {
        return input.replace("{stock-name}", stock.getName())
                .replace("{stock-id}", stock.getId())
                .replace("{current-price}", Stonks.plugin.configManager.stockPriceFormat.format(stock.getPrice()))
                .replace("{hour-evolution}", Utils.formatRate(stock.getEvolution(TimeScale.HOUR)))
                .replace("{day-evolution}", Utils.formatRate(stock.getEvolution(TimeScale.DAY)))
                .replace("{week-evolution}", Utils.formatRate(stock.getEvolution(TimeScale.WEEK)))
                .replace("{stock-type}", stock.getHandler() instanceof RealStockHandler ? "Real Stock" : "Virtual");
    }
}
