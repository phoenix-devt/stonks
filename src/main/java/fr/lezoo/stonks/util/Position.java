package fr.lezoo.stonks.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Objects;

/**
 * A hashable position object which
 * can be used as a hashMap key type
 *
 * @author jules
 */
public class Position {
    private final World world;
    private final int x, y, z;

    public Position(Location loc) {
        this(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Position(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public World getWorld() {
        return world;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    public static Position from(Location loc) {
        return new Position(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Position from(ConfigurationSection config) {
        World world = Objects.requireNonNull(Bukkit.getWorld(config.getString("world")), "Could not find world");
        return new Position(world, config.getInt("x"), config.getInt("y"), config.getInt("z"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return x == position.x && y == position.y && z == position.z && world.equals(position.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, x, y, z);
    }
}
