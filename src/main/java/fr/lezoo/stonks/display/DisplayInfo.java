package fr.lezoo.stonks.display;

import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationTimeDisplay;


//All the information needed to create a display
public class DisplayInfo {
    private final Quotation quotation;
    private final QuotationTimeDisplay timeDisplay;

    public QuotationTimeDisplay getTimeDisplay() {
        return timeDisplay;
    }

    public Quotation getQuotation() {
        return quotation;
    }

    public DisplayInfo(Quotation quotation, QuotationTimeDisplay timeDisplay) {
        this.quotation = quotation;
        this.timeDisplay = timeDisplay;
    }
}
