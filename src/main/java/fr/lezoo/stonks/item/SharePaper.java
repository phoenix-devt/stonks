package fr.lezoo.stonks.item;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.share.Share;
import fr.lezoo.stonks.util.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class SharePaper extends CustomItem<Share> {
    public SharePaper(ConfigurationSection config) {
        super(config);
    }

    @Override
    public Placeholders getPlaceholders(Player player, Share share) {
        Placeholders placeholders = new Placeholders();

        placeholders.register("amount", Utils.fourDigits.format(share.getOrderInfo().getAmount()));
        placeholders.register("leverage", Utils.fourDigits.format(share.getOrderInfo().getLeverage()));
        placeholders.register("initial-price", Stonks.plugin.configManager.stockPriceFormat.format(share.getInitialPrice()));
        placeholders.register("date", Stonks.plugin.configManager.dateFormat.format(share.getCreationTime()));
        placeholders.register("quotation-name", share.getQuotation().getName());
        placeholders.register("type", share.getType().getTranslation());

        return placeholders;
    }

    @Override
    public void whenBuilt(ItemStack item, ItemMeta meta, Share share) {
        PersistentDataContainer nbt = meta.getPersistentDataContainer();
        nbt.set(Utils.namespacedKey("StockId"), PersistentDataType.STRING, share.getQuotation().getId());
        nbt.set(Utils.namespacedKey("ShareTimeStamp"), PersistentDataType.LONG, share.getCreationTime());
        nbt.set(Utils.namespacedKey("ShareAmount"), PersistentDataType.DOUBLE, share.getOrderInfo().getAmount());
        nbt.set(Utils.namespacedKey("ShareLeverage"), PersistentDataType.INTEGER, share.getOrderInfo().getLeverage());
        nbt.set(Utils.namespacedKey("ShareType"), PersistentDataType.STRING, share.getType().name());
        nbt.set(Utils.namespacedKey("ShareInitialPrice"), PersistentDataType.DOUBLE, share.getInitialPrice());
        nbt.set(Utils.namespacedKey("ShareWallet"), PersistentDataType.DOUBLE, share.getWallet());
        item.setItemMeta(meta);
    }
}
