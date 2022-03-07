package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.objects.EditableInventory;
import fr.lezoo.stonks.gui.objects.GeneratedInventory;
import fr.lezoo.stonks.gui.objects.item.InventoryItem;
import fr.lezoo.stonks.gui.objects.item.Placeholders;
import fr.lezoo.stonks.gui.objects.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;
import fr.lezoo.stonks.share.OrderInfo;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.ChatInput;
import fr.lezoo.stonks.util.ConfigFile;
import fr.lezoo.stonks.util.InputHandler;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.text.DecimalFormat;

/**
 * Menu where you can buy or short shares for a specific quotation.
 */
public class QuotationShareMenu extends EditableInventory {
    public QuotationShareMenu() {
        super("share-menu");
    }

    @Override
    public InventoryItem loadItem(String function, ConfigurationSection config) {

        if (function.equalsIgnoreCase("buy-custom") || function.equalsIgnoreCase("sell-custom"))
            return new CustomActionItem(config);

        if (function.startsWith("info"))
            return new QuotationInfoItem(config);

        if (function.startsWith("leverage"))
            return new LeverageItem(config);

        if (function.startsWith("buy"))
            return new BuyShareItem(config);

        if (function.startsWith("sell"))
            return new SellShareItem(config);

        if (function.startsWith("max-price"))
            return new MaxPriceItem(config);

        if (function.startsWith("min-price"))
            return new MinPriceItem(config);

        return new SimpleItem(config);
    }

    public GeneratedInventory generate(PlayerData player, Quotation quotation) {
        return new GeneratedShareMenu(player, this, quotation);
    }

    public class GeneratedShareMenu extends GeneratedInventory {
        private final Quotation quotation;

        public GeneratedShareMenu(PlayerData playerData, EditableInventory editable, Quotation quotation) {
            super(playerData, editable);
            this.quotation = quotation;
            //We change the current quotation of the player to the actual one
            playerData.setCurrentQuotation(quotation);
        }

        public Quotation getQuotation() {
            return quotation;
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{name}", quotation.getName());
        }

        @Override
        public void whenClicked(InventoryClickEvent event, InventoryItem item) {

            // Market is closing!
            if (Stonks.plugin.isClosed()) {
                Message.MARKET_CLOSING.format().send(player);
                player.closeInventory();
                return;
            }

            if (item.getFunction().equals("back")) {
                Stonks.plugin.configManager.QUOTATION_LIST.generate(playerData).open();
                return;
            }

            if (item instanceof LeverageItem) {
                Message.SET_LEVERAGE_ASK.format().send(player);
                new ChatInput(this, InputHandler.SET_LEVERAGE_HANDLER);
            }
            if (item instanceof MinPriceItem) {
                Message.SET_MIN_PRICE_ASK.format().send(player);
                new ChatInput(this, InputHandler.SET_MIN_PRICE_HANDLER);
            }
            if (item instanceof MaxPriceItem) {
                Message.SET_MAX_PRICE_ASK.format().send(player);
                new ChatInput(this, InputHandler.SET_MAX_PRICE_HANDLER);
            }

            //We buy the shares using the leverage,minPrice,maxPrice provided
            if (item instanceof AmountActionItem)
                playerData.buyShare(quotation, item instanceof BuyShareItem ? ShareType.NORMAL : ShareType.SHORT,((AmountActionItem) item).getAmount());

            if (item instanceof CustomActionItem) {
                ShareType type = item.getFunction().equalsIgnoreCase("buy-custom") ? ShareType.NORMAL : ShareType.SHORT;
                (type == ShareType.NORMAL ? Message.BUY_CUSTOM_ASK : Message.SELL_CUSTOM_ASK).format().send(player);

                //We set the currentQuotation to be the current one to set properly the amount
                playerData.setCurrentQuotation(quotation);

                if (type == ShareType.NORMAL) {
                    new ChatInput(this, InputHandler.BUY_CUSTOM_AMOUNT_HANDLER);
                } else {
                    new ChatInput(this, InputHandler.SHORT_CUSTOM_AMOUNT_HANDLER);
                }

            }


        }

        @Override
        public void whenClosed(InventoryCloseEvent event) {
            // Nothing
        }
    }

    /**
     * Used to reduce code multiplication
     */
    public interface AmountActionItem {
        int getAmount();
    }

    /**
     * Item when selling a specific amount of shares
     */
    public class SellShareItem extends InventoryItem<GeneratedShareMenu> implements AmountActionItem {
        private final int amount;

        public SellShareItem(ConfigurationSection config) {
            super(config);

            this.amount = Integer.parseInt(getFunction().substring(4));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();
            OrderInfo orderInfo = inv.getPlayerData().getOrderInfo(inv.getQuotation().getId());
            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.fourDigits.format(orderInfo.getLeverage()));
            holders.register("min-price", orderInfo.getStringMinPrice());
            holders.register("max-price", orderInfo.getStringMaxPrice());
            holders.register("amount", amount);

            return holders;
        }

        @Override
        public int getAmount() {
            return amount;
        }
    }

    /**
     * Item when buying a specific amount of shares
     */
    public class BuyShareItem extends InventoryItem<GeneratedShareMenu> implements AmountActionItem {
        private final int amount;

        public BuyShareItem(ConfigurationSection config) {
            super(config);

            this.amount = Integer.parseInt(getFunction().substring(3));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();
            OrderInfo orderInfo = inv.getPlayerData().getOrderInfo(inv.getQuotation().getId());
            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.fourDigits.format(orderInfo.getLeverage()));
            holders.register("min-price", orderInfo.getStringMinPrice());
            holders.register("max-price", orderInfo.getStringMaxPrice());
            holders.register("amount", amount);

            return holders;
        }

        @Override
        public int getAmount() {
            return amount;
        }
    }

    /**
     * Item when buying or short selling a custom amount of shares
     */
    public class CustomActionItem extends InventoryItem<GeneratedShareMenu> {
        public CustomActionItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();
            OrderInfo orderInfo = inv.getPlayerData().getOrderInfo(inv.getQuotation().getId());
            holders.register("leverage", Utils.fourDigits.format(orderInfo.getLeverage()));
            holders.register("min-price", orderInfo.getStringMinPrice());
            holders.register("max-price", orderInfo.getStringMaxPrice());
            return holders;
        }
    }

    public class LeverageItem extends InventoryItem<GeneratedShareMenu> {
        public LeverageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();
            OrderInfo orderInfo = inv.getPlayerData().getOrderInfo(inv.getQuotation().getId());
            holders.register("leverage", Utils.fourDigits.format(orderInfo.getLeverage()));
            holders.register("min-price", orderInfo.getStringMinPrice());
            holders.register("max-price", orderInfo.getStringMaxPrice());
            return holders;
        }
    }

    public class MinPriceItem extends InventoryItem<GeneratedShareMenu> {
        public MinPriceItem(ConfigurationSection config) {
            super(config);
        }

        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders placeholders = new Placeholders();
            placeholders.register("min-price", inv.getPlayerData().getOrderInfo(inv.getQuotation().getId()).getStringMinPrice());
            return placeholders;
        }

    }

    public class MaxPriceItem extends InventoryItem<GeneratedShareMenu> {
        public MaxPriceItem(ConfigurationSection config) {
            super(config);
        }

        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders placeholders = new Placeholders();
            placeholders.register("max-price", inv.getPlayerData().getOrderInfo(inv.getQuotation().getId()).getStringMaxPrice());
            return placeholders;
        }
    }


    public class QuotationInfoItem extends InventoryItem<GeneratedShareMenu> {
        public QuotationInfoItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("name", inv.quotation.getName());

            holders.register("week-low", format.format(inv.quotation.getLowest(TimeScale.WEEK)));
            holders.register("week-high", format.format(inv.quotation.getHighest(TimeScale.WEEK)));
            holders.register("month-low", format.format(inv.quotation.getLowest(TimeScale.MONTH)));
            holders.register("month-high", format.format(inv.quotation.getHighest(TimeScale.MONTH)));

            // TODO instead of comparing to 1 day ago, compare to the beginning of the day, same with month, year..
            holders.register("hour-evolution", Utils.formatRate(inv.quotation.getEvolution(TimeScale.HOUR)));
            holders.register("day-evolution", Utils.formatRate(inv.quotation.getEvolution(TimeScale.DAY)));
            holders.register("week-evolution", Utils.formatRate(inv.quotation.getEvolution(TimeScale.WEEK)));
            holders.register("month-evolution", Utils.formatRate(inv.quotation.getEvolution(TimeScale.MONTH)));

            return holders;
        }
    }
}
