package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.quotation.Quotation;
import org.jetbrains.annotations.NotNull;

public interface QuotationInventory {

    @NotNull
    public Quotation getQuotation();
}
