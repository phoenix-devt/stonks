package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.api.share.ShareType;
import fr.lezoo.stonks.api.util.ChatInput;
import fr.lezoo.stonks.api.util.Utils;
import fr.lezoo.stonks.api.util.message.Message;
import fr.lezoo.stonks.gui.api.EditableInventory;
import fr.lezoo.stonks.gui.api.GeneratedInventory;
import fr.lezoo.stonks.gui.api.item.InventoryItem;
import fr.lezoo.stonks.gui.api.item.PlaceholderItem;
import fr.lezoo.stonks.gui.api.item.Placeholders;
import fr.lezoo.stonks.gui.api.item.SimplePlaceholderItem;
import org.bukkit.ChatColor;
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

        return new SimplePlaceholderItem(config);
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
                ShareType type = item.getFunction().equalsIgnoreCase("buy-custom") ? ShareType.POSITIVE : ShareType.SHORT;
                (type == ShareType.POSITIVE ? Message.BUY_CUSTOM_ASK : Message.SELL_CUSTOM_ASK).format().send(player);

                new ChatInput(this, (playerData, input) -> {
                    double amount;
                    try {
                        amount = Double.parseDouble(input);
                    } catch (IllegalArgumentException exception) {
                        Message.NOT_VALID_NUMBER.format("input", input).send(player);
                        return false;
                    }

                    playerData.buyShare(quotation, type, amount);
                    return true;
                });
            }

            if (item instanceof AmountActionItem)
                playerData.buyShare(quotation, item instanceof BuyShareItem ? ShareType.POSITIVE : ShareType.SHORT, ((AmountActionItem) item).getAmount());
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
    public class SellShareItem extends PlaceholderItem<GeneratedShareMenu> implements AmountActionItem {
        private final int amount;

        public SellShareItem(ConfigurationSection config) {
            super(config);

            this.amount = Integer.parseInt(getFunction().substring(4));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.singleDigit.format(inv.getPlayerData().getLeverage()));
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
    public class BuyShareItem extends PlaceholderItem<GeneratedShareMenu> implements AmountActionItem {
        private final int amount;

        public BuyShareItem(ConfigurationSection config) {
            super(config);

            this.amount = Integer.parseInt(getFunction().substring(3));
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("price", Stonks.plugin.configManager.stockPriceFormat.format(inv.quotation.getPrice() * amount));
            holders.register("leverage", Utils.singleDigit.format(inv.getPlayerData().getLeverage()));
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
    public class CustomActionItem extends PlaceholderItem<GeneratedShareMenu> {
        public CustomActionItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("leverage", Utils.singleDigit.format(inv.getPlayerData().getLeverage()));

            return holders;
        }
    }

    public class LeverageItem extends PlaceholderItem<GeneratedShareMenu> {
        public LeverageItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            holders.register("leverage", Utils.singleDigit.format(inv.getPlayerData().getLeverage()));

            return holders;
        }
    }

    public class QuotationInfoItem extends PlaceholderItem<GeneratedShareMenu> {
        public QuotationInfoItem(ConfigurationSection config) {
            super(config);
        }

        @Override
        public Placeholders getPlaceholders(GeneratedShareMenu inv, int n) {
            Placeholders holders = new Placeholders();

            DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;

            holders.register("company-name", inv.quotation.getCompanyName());
            holders.register("stock-name", inv.quotation.getStockName());

            holders.register("week-low", format.format(inv.quotation.getLowest(QuotationInfo.WEEK_TIME_OUT)));
            holders.register("week-high", format.format(inv.quotation.getHighest(QuotationInfo.WEEK_TIME_OUT)));
            holders.register("month-low", format.format(inv.quotation.getLowest(QuotationInfo.MONTH_TIME_OUT)));
            holders.register("month-high", format.format(inv.quotation.getHighest(QuotationInfo.MONTH_TIME_OUT)));

            // TODO instead of comparing to 1 day ago, compare to the beginning of the day, same with month, year..
            holders.register("hour-evolution", formatEvolution(inv.quotation.getEvolution(QuotationInfo.HOUR_TIME_OUT)));
            holders.register("day-evolution", formatEvolution(inv.quotation.getEvolution(QuotationInfo.DAY_TIME_OUT)));
            holders.register("week-evolution", formatEvolution(inv.quotation.getEvolution(QuotationInfo.WEEK_TIME_OUT)));
            holders.register("month-evolution", formatEvolution(inv.quotation.getEvolution(QuotationInfo.MONTH_TIME_OUT)));

            return holders;
        }
    }

    private String formatEvolution(double growthRate) {
        if (growthRate == 0)
            return ChatColor.WHITE + "0";

        DecimalFormat format = Stonks.plugin.configManager.stockPriceFormat;
        if (growthRate < 0)
            return ChatColor.RED + format.format(growthRate) + "%";
        return ChatColor.GREEN + "+" + format.format(growthRate) + "%";
    }
}
