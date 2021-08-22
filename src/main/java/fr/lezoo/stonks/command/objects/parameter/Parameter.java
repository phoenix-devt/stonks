package fr.lezoo.stonks.command.objects.parameter;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeExplorer;
import fr.lezoo.stonks.quotation.TimeScale;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.function.BiConsumer;

public class Parameter {
    private final String key;
    private final BiConsumer<CommandTreeExplorer, List<String>> autoComplete;

    public static final Parameter PLAYER = new Parameter("<player>",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
    public static final Parameter PLAYER_OPTIONAL = new Parameter("(player)",
            (explorer, list) -> Bukkit.getOnlinePlayers().forEach(online -> list.add(online.getName())));
    public static final Parameter QUOTATION_ID = new Parameter("<quotationId>", (explorer, list) -> {
        Stonks.plugin.quotationManager.getQuotations().forEach(quot -> list.add(quot.getId()));
    });
    public static final Parameter TIME_SCALE = new Parameter("<timeScale>", (explorer, list) -> {
        for (TimeScale display : TimeScale.values())
            list.add(display.name());
    });

    public Parameter(String key, BiConsumer<CommandTreeExplorer, List<String>> autoComplete) {
        this.key = key;
        this.autoComplete = autoComplete;
    }

    public String getKey() {
        return key;
    }

    public void autoComplete(CommandTreeExplorer explorer, List<String> list) {
        autoComplete.accept(explorer, list);
    }
}
