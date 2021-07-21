package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.gui.item.InventoryItem;
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

	public InventoryItem getByFunction(String function) {
		for (InventoryItem item : loaded)
			if (item.getFunction().equals(function))
				return item;
		return null;
	}

	public InventoryItem getBySlot(int slot) {
		for (InventoryItem item : loaded)
			if (item.getSlots().contains(slot))
				return item;
		return null;
	}

	/*
	 * this method must use an ordered collection because of GUI items
	 * overriding possibilities.
	 */
	public void addLoaded(InventoryItem item) {
		loaded.add(0, item);
	}

	@Override
	public Inventory getInventory() {
		Inventory inv = Bukkit.createInventory(this, editable.getSlots(), Stonks.plugin.placeholderParser.parse(player, calculateName()));

		for (InventoryItem item : editable.getItems())
			if (item.canDisplay(this))
				item.setDisplayed(inv, this);

		return inv;
	}

	public void open() {

		/*
		 * Very important, in order to prevent ghost items, the loaded items map
		 * must be cleared when the inventory is updated or open at least twice
		 */
		loaded.clear();

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

	public abstract String calculateName();

	public abstract void whenClicked(InventoryClickEvent event, InventoryItem item);
}
