package fr.lezoo.stonks.display.board;

import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.TimeScale;

/**
 * All the information needed to create a display board
 */
public class DisplayInfo {
    private final Stock stock;
    private final TimeScale timeDisplay;

    public TimeScale getTimeDisplay() {
        return timeDisplay;
    }

    public Stock getStock() {
        return stock;
    }

    public DisplayInfo(Stock stock, TimeScale timeDisplay) {
        this.stock = stock;
        this.timeDisplay = timeDisplay;
    }
}
