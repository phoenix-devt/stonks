package fr.lezoo.stonks.api.quotation;

public class QuotationInfo {
    private final long timeStamp;
    private final double price;

    public QuotationInfo(long time, double price) {
        this.timeStamp=time;
        this.price=price;
    }


}
