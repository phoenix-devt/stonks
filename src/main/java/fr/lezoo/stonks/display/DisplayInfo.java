package fr.lezoo.stonks.display;

import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.TimeScale;

/**
 * All the information needed to create a display
 */
public class DisplayInfo {
    private final Quotation quotation;
    private final TimeScale timeDisplay;

    public TimeScale getTimeDisplay() {
        return timeDisplay;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public DisplayInfo(Quotation quotation, TimeScale timeDisplay) {
        this.quotation = quotation;
        this.timeDisplay = timeDisplay;
    }
}
