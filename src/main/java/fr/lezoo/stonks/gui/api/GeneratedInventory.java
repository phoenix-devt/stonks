package fr.lezoo.stonks.gui.api;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public abstract class GeneratedInventory extends PluginInventory {
    private final EditableInventory editable;
    private final List<InventoryItem> loaded = new ArrayList<>();

    public GeneratedInventory(PlayerData playerData, EditableInventory editable) {
        super(playerData);

        this.editable = editable;
    }

    public List<InventoryItem> getLoaded() {
        return loaded;
    }

    public EditableInventory getEditable() {
        return editable;
    }

    /**
     * @param function The item function, like 'next-page'
     * @return Item with corresponding function, or null if none was found
     */
    public InventoryItem getByFunction(String function) {

        for (InventoryItem item : loaded)
            if (item.getFunction().equals(function))
                return item;

        return null;
    }

    /**
     * @param slot The item slot
     * @return Item with corresponding slot, or null of none was found
     */
    public InventoryItem getBySlot(int slot) {

        for (InventoryItem item : loaded)
            if (item.getSlots().contains(slot))
                return item;

        return null;
    }


    /**
     * Order matters in the loaded array; if the user uses two
     * items with the same slot, we want the last item to be generated
     * to be the first chosen when using {@link #getByFunction(String)}
     * or {@link #getBySlot(int)}.
     *
     * @param item Registers an item that was added
     */
    public void addLoaded(InventoryItem item) {
        loaded.add(0, item);
    }

    @Override
    public Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, editable.getSlots(), Stonks.plugin.placeholderParser.parse(player, calculateName()));

        for (InventoryItem item : editable.getItems())
            if (item.isDisplayed(this))
                item.display(inv, this);

        return inv;
    }

    public void open() {

        /*
         * Very important, in order to prevent ghost items, the loaded items map
         * must be cleared when the inventory is updated or open at least twice.
         *
         * This method is useless if the inventory is opened for the first time,
         * but since the same inventory can be opened for instance when changing
         * page, we DO need to clear this first.
         */
        loaded.clear();

        // Only then we open the inventory
        getPlayer().openInventory(getInventory());
    }

    public void whenClicked(InventoryClickEvent event) {
        event.setCancelled(true);

        if (event.getClickedInventory() != null && event.getClickedInventory().equals(event.getInventory())) {
            InventoryItem item = getBySlot(event.getSlot());
            if (item == null)
                return;

			/*if (item instanceof TriggerItem)
				((TriggerItem) item).getTrigger().apply(getPlayerData());
			else*/
            whenClicked(event, item);
        }
    }

    /**
     * The name of the inventory depends on the state of the inventory.
     * If the current page is 4 and if the max amount of pages is 6,
     * the inventory name should return 'Quotations (4/6)'
     *
     * @return The name of the inventory.
     */
    public abstract String calculateName();

    public abstract void whenClicked(InventoryClickEvent event, InventoryItem item);
}
