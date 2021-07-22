package fr.lezoo.stonks.api.quotation;


/**
 * Place where players can buy and sell stocks
 */
public abstract class Quotation {
    private final String id;

    private double stockPrice;

    public Quotation(String id) {
        this.id = id;
    }
}
