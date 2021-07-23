package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.api.quotation.Quotation;
import org.apache.commons.lang.Validate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class QuotationManager {
    private final Map<String, Quotation> map = new HashMap<>();

    public Quotation get(String id) {
        return map.get(id);
    }

    public void register(Quotation quotation) {
        Validate.isTrue(!map.containsKey(quotation.getId()), "There is already a quotation with ID '" + quotation.getId() + "'");

        map.put(quotation.getId(), quotation);
    }

    /**
     *
     * @param id of quotation
     * @return if such a quotation exists
     */
    public boolean hasId(String id) {
        return map.containsKey(id);
    }

    public Collection<Quotation> getQuotations() {
        return map.values();
    }
}
