package fr.lezoo.stonks.version;

/**
 * An item NBT tag. Object types supported for {@link #value}
 * are strings, booleans and doubles.
 *
 * @author Jules
 */
public class ItemTag {
    private final String path;
    private final Object value;

    public ItemTag(String path, Object value) {
        this.path = path;
        this.value = value;
    }

    public String getPath() {
        return path;
    }

    public Object getValue() {
        return value;
    }
}