package fr.lezoo.stonks.command.objects.parameter;

public class NumericalParameter extends Parameter {
    public NumericalParameter(String key, int... values) {
        super(key, (explorer, list) -> {
            for (int value : values)
                list.add(String.valueOf(value));
        });
    }
}
