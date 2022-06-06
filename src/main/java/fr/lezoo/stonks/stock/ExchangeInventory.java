package fr.lezoo.stonks.stock;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ExchangeInventory {
    private final ExchangeType type;
    private final List<ItemStack> items = new ArrayList<>();

    public ExchangeInventory(ExchangeType type, Player player) {
        this.type = type;

        // Loop through the player's inventory
        for (ItemStack item : player.getInventory().getContents())
            if (type.matches(item))
                items.add(item);
    }

    public void takeOff(long amount) {
        while (amount > 0) {
            Validate.isTrue(!items.isEmpty(), "Could not take off " + amount + " items because none were found");

            final ItemStack found = items.get(0);
            final long taken = Math.min(amount, found.getAmount());
            final int newAmount = (int) (found.getAmount() - taken);
            amount -= taken; // Reduce counter

            // Take off items
            found.setAmount(newAmount);

            // Unregister from inventory
            if (newAmount == 0)
                items.remove(0);
        }
    }

    public int computeTotal() {
        int t = 0;
        for (ItemStack stack : items)
            t += stack.getAmount();
        return t;
    }
}
