package fr.lezoo.stonks.api.util.message;

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


    BUY_CUSTOM_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eWrite in the chat the amount of shares you would like to buy."),
    SELL_CUSTOM_ASK(new SoundReader(Sound.ENTITY_PLAYER_LEVELUP, 1, 2), "&eWrite in the chat the amount of shares you would like to buy."),

    NOT_VALID_NUMBER("&c{input} is not a valid number."),
    NOT_VALID_LEVERAGE("&cThe leverage must be a positive number"),
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
     * @return A copy of the default message with no parsed placeholders
     */
    public List<String> getDefault() {
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