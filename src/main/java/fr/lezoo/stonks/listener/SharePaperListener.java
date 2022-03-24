package fr.lezoo.stonks.listener;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SharePaperListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void a(PlayerInteractEvent event) {

        if (!event.getAction().name().contains("RIGHT_CLICK") || !event.hasItem())
            return;

        ItemStack item = event.getItem();
        if (!item.hasItemMeta())
            return;

        PersistentDataContainer container = item.getItemMeta().getPersistentDataContainer();
        if (!container.has(Utils.namespacedKey("ShareType"), PersistentDataType.STRING))
            return;

        // Find share and give to player
        Player player = event.getPlayer();
        Share share = new Share(player.getUniqueId(), container);
        share.getOrderInfo().setAmount(share.getOrderInfo().getAmount() * item.getAmount());

        // Clear item and give share afterwards
        player.getInventory().setItem(event.getHand(), null);
        PlayerData playerData = PlayerData.get(player);

        playerData.giveShare(share);
        Message.CLAIM_SHARE_PAPER.format("shares", Utils.fourDigits.format(share.getOrderInfo().getAmount()),
                "name", share.getQuotation().getName(),
                "value", Stonks.plugin.configManager.stockPriceFormat.format(share.getCloseEarning(playerData.getTaxRate()))).send(player);
    }
}
