package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.api.quotation.Quotation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QuotationManager {
    private final Map<String, Quotation> map = new HashMap<>();

    public Quotation get(String id) {
        return map.get(id);
    }

    public Collection<Quotation> getQuotations() {
        return map.values();
    }
}
