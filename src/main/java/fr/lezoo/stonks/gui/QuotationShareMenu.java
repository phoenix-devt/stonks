package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimpleItem;
import fr.lezoo.stonks.player.PlayerData;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationTimeDisplay;
import fr.lezoo.stonks.share.ShareType;
import fr.lezoo.stonks.util.ChatInput;
import fr.lezoo.stonks.util.Utils;
import fr.lezoo.stonks.util.message.Message;
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
        }

        @Override
        public String applyNamePlaceholders(String str) {
            return str.replace("{company}", quotation.getCompanyName());
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

            if (item instanceof LeverageItem)
                new ChatInput(this, (playerData, input) -> {
                    double amount;
                    try {
                        amount = Double.parseDouble(input);
                    } catch (IllegalArgumentException exception) {
                        Message.NOT_VALID_NUMBER.format("input", input).send(player);
                        return false;
                    }

                    if (amount <= 0) {
                        Message.NOT_VALID_LEVERAGE.format("input", input).send(player);
                        return false;
                    }

                    playerData.setLeverage(amount);
                    return true;
                });

            if (item instanceof CustomActionItem) {
                ShareType type = item.getFunction().equalsIgnoreCase("buy-custom") ? ShareType.NORMAL : ShareType.SHORT;
                (type == ShareType.NORMAL ? Message.BUY_CUSTOM_ASK : Message.SELL_CUSTOM_ASK).format().send(player);

                new ChatInput(this, (playerData, input) -> {
                    double amount;
                    try {
                        amount = Double.parseDouble(input);
                    } catch (IllegalArgumentException exception) {
                        Message.NOT_VALID_NUMBER.format("input", input).send(player);
                        return false;
                    }

                    playerData.buyShare(quotation, type, amount, -1, -1);
                    return true;
                });
            }

            if (item instanceof AmountActionItem)
                playerData.buyShare(quotation, item instanceof BuyShareItem ? ShareType.NORMAL : ShareType.SHORT, ((AmountActionItem) item).getAmount(), -1, -1);
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
        public int getAmount();
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

            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.fourDigits.format(inv.getPlayerData().getLeverage()));
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

            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.fourDigits.format(inv.getPlayerData().getLeverage()));
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

            holders.register("leverage", Utils.fourDigits.format(inv.getPlayerData().getLeverage()));

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

            holders.register("leverage", Utils.fourDigits.format(inv.getPlayerData().getLeverage()));

            return holders;
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

            holders.register("company-name", inv.quotation.getCompanyName());
            holders.register("stock-name", inv.quotation.getStockName());

            holders.register("week-low", format.format(inv.quotation.getLowest(QuotationTimeDisplay.WEEK)));
            holders.register("week-high", format.format(inv.quotation.getHighest(QuotationTimeDisplay.WEEK)));
            holders.register("month-low", format.format(inv.quotation.getLowest(QuotationTimeDisplay.MONTH)));
            holders.register("month-high", format.format(inv.quotation.getHighest(QuotationTimeDisplay.MONTH)));

            // TODO instead of comparing to 1 day ago, compare to the beginning of the day, same with month, year..
            holders.register("hour-evolution", Utils.formatRate(inv.quotation.getEvolution(QuotationTimeDisplay.HOUR)));
            holders.register("day-evolution", Utils.formatRate(inv.quotation.getEvolution(QuotationTimeDisplay.DAY)));
            holders.register("week-evolution", Utils.formatRate(inv.quotation.getEvolution(QuotationTimeDisplay.WEEK)));
            holders.register("month-evolution", Utils.formatRate(inv.quotation.getEvolution(QuotationTimeDisplay.MONTH)));

            return holders;
        }
    }
}
