package fr.lezoo.stonks.gui.api.item;

import fr.lezoo.stonks.gui.api.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class InventoryItem<T extends GeneratedInventory> {
    private final String id, function;
    private final List<Integer> slots = new ArrayList<>();

    public InventoryItem(ConfigurationSection config) {
        this(config.getName(), config.getString("function"));

        config.getStringList("slots").forEach(str -> slots.add(Integer.parseInt(str)));
    }

    public InventoryItem(String id, String function) {
        this.id = id;
        this.function = function == null ? "" : function.toLowerCase();
    }

    public String getId() {
        return id;
    }

    public String getFunction() {
        return function;
    }

    public boolean hasFunction() {
        return !function.isEmpty();
    }

    public List<Integer> getSlots() {
        return slots;
    }

    public boolean hasDifferentDisplay() {
        return false;
    }

    public void display(Inventory inv, T generated) {
        generated.addLoaded(this);

        if (!hasDifferentDisplay()) {
            ItemStack display = getDisplayedItem(generated, 0);
            for (int slot : getSlots())
                inv.setItem(slot, display);

        } else
            for (int j = 0; j < slots.size(); j++)
                inv.setItem(slots.get(j), getDisplayedItem(generated, j));

    }

    /**
     * @param inv Generated inventory being opened by a player
     * @param n   Some items are grouped, like the item 'quotation' in the quotation list
     *            as they are multiple quotations to display yet only ONE inventory item
     *            gives the template. This is the index of the item being displayed.
     * @return Item that will be displayed in the generated inventory
     */
    public abstract ItemStack getDisplayedItem(T inv, int n);

    /**
     * @param inv Generated inventory
     * @return If the item can be displayed in this inventory
     */
    public abstract boolean isDisplayed(T inv);
}
