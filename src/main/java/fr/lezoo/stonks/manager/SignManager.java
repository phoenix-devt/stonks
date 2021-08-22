package fr.lezoo.stonks.manager;

import fr.lezoo.stonks.display.sign.DisplaySign;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.util.Position;
import org.apache.commons.lang.Validate;

import java.util.*;

public class SignManager implements FileManager {
    private final Map<Position, DisplaySign> mapped = new HashMap<>();

    public boolean has(Position position) {
        return mapped.containsKey(position);
    }

    public Collection<DisplaySign> getActive() {
        return mapped.values();
    }

    public void register(DisplaySign sign) {
        Validate.isTrue(!mapped.containsKey(sign.getPosition()), "Cannot register two signs at the same position");

        mapped.put(sign.getPosition(), sign);
    }

    public void unregister(Position pos) {
        mapped.remove(pos);
    }

    /**
     * @return Current display signs linked to given quotation
     */
    public Set<DisplaySign> getByQuotation(Quotation quotation) {
        Set<DisplaySign> signs = new HashSet<>();

        for (DisplaySign sign : mapped.values())
            if (sign.getQuotation().equals(quotation))
                signs.add(sign);

        return signs;
    }

    @Override
    public void load() {

        // TODO
    }

    @Override
    public void save() {

        // TODO
    }
}
