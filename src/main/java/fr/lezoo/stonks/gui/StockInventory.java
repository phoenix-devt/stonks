package fr.lezoo.stonks.gui;

import fr.lezoo.stonks.stock.Stock;
import org.jetbrains.annotations.NotNull;

public interface StockInventory {

    @NotNull
    public Stock getStock();
}
