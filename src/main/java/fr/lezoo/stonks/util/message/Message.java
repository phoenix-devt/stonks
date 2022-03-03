package fr.lezoo.stonks.util.message;

import org.apache.commons.lang.Validate;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Message {
    NOT_ENOUGH_MONEY("&cYou don't have enough money: you need ${left} more"),
    BUY_SHARES(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eYou bought {shares} shares from {company} for ${price}."),
    SELL_SHARES(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eYou short-sold {shares} shares from {company} for ${price}."),
    CLOSE_SHARES(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eYou closed {shares} shares from {company} for a final gain of ${gain}."),
    GET_SHARE_PAPER(new SoundReader(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2), "&eYou virtually closed your order and got an order paper for {shares} shares of {company}."),
    CLAIM_SHARE_PAPER(new SoundReader(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2), "&eYou claimed {shares} shares of {company} for a total current value of ${value}."),

    CLAIM_DIVIDENDS(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eYou successfully redeemed &f${amount} &efrom your dividends."),
    NO_DIVIDENDS_TO_CLAIM("&cYou don't have any dividends to claim."),

    MARKET_CLOSING("&cThe stock market is closed for now."),

    SET_PARAMETER_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP,1,2),"&e[1]Leverage: {leverage}\n" +
            "[2]Amount:{amount}\n" +
            "*opt*[3]Min price: {min-price}\n" +
            "*opt[4]Max price: {max-price}\n" +
            "[100]Save"),
    SET_LEVERAGE_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2),"&eWrite in the chat the leverage you want to have "),
    SET_AMOUNT_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2),"&eWrite in the chat the amount of shares you want"),
    SET_MIN_PRICE_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2),"&eWrite in the chat the min-price you want your share to automatically sell at"),
    SET_MAX_PRICE_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2),"&&eWrite in the chat the max-price you want your share to automatically sell at"),

    BUY_CUSTOM_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eWrite in the chat the amount of shares you would like to buy."),
    SELL_CUSTOM_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eWrite in the chat the amount of shares you would like to buy."),
    NOT_VALID_NUMBER("&c{input} is not a valid number."),
    NOT_VALID_AMOUNT("&c{input} is not a valid amount."),
    NOT_VALID_MIN_PRICE("&c{input} is not a valid min price."),
    NOT_VALID_MAX_PRICE("&c{input} is not a valid max price."),
    NOT_VALID_LEVERAGE("&cThe leverage must be a positive number"),
    NOT_ENOUGH_PERMISSIONS("&cYou don't have enough permissions."),
    ;

    private List<String> message;
    private SoundReader sound;

    private Message(String... message) {
        this(null, message);
    }

    private Message(SoundReader sound, String... message) {
        this.message = Arrays.asList(message);
        this.sound = sound;
    }

    public String getPath() {
        return name().toLowerCase().replace("_", "-");
    }

    /**
     * Deep Copy !!
     *
     * @return Message updated based on what's in the config files
     */
    public List<String> getCached() {
        return new ArrayList<>(message);
    }

    public SoundReader getSound() {
        return sound;
    }

    public boolean hasSound() {
        return sound != null;
    }

    public PlayerMessage format(Object... placeholders) {
        return new PlayerMessage(this).format(placeholders);
    }

    public void update(ConfigurationSection config) {
        List<String> format = config.getStringList("format");
        Validate.notNull(this.message = format, "Could not read message format");
        sound = config.contains("sound") ? new SoundReader(config.getConfigurationSection("sound")) : null;
    }
}