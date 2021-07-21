package fr.lezoo.stonks.gui.item;

import fr.lezoo.stonks.gui.GeneratedInventory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public abstract class InventoryItem {
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

	public void setDisplayed(Inventory inv, GeneratedInventory generated) {
		generated.addLoaded(this);

		if (!hasDifferentDisplay()) {
			ItemStack display = display(generated);
			for (int slot : getSlots())
				inv.setItem(slot, display);
		}

		else
			for (int j = 0; j < slots.size(); j++)
				inv.setItem(slots.get(j), display(generated, j));

	}

	public ItemStack display(GeneratedInventory inv) {
		return display(inv, 0);
	}

	public abstract ItemStack display(GeneratedInventory inv, int n);

	public abstract boolean canDisplay(GeneratedInventory inv);
}
