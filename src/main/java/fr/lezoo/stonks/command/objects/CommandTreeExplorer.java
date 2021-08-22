package fr.lezoo.stonks.command.objects;

import java.util.List;

public class CommandTreeExplorer {
    private final String[] args;

    private CommandTreeNode current;
    private int parameter = 0;

    /**
     * Used to explore a command tree given a certain command
     *
     * @param explored
     *            The command tree to explore
     * @param args
     *            Given arguments, tells what direction to take at every tree
     *            node
     */
    public CommandTreeExplorer(CommandTreeRoot explored, String[] args) {
        this.current = explored;
        this.args = args;

        for (String arg : args)

            /*
             * Check if current command floor has the corresponding arg, if so
             * let the next floor handle the command.
             */
            if (parameter == 0 && current.hasChild(arg))
                current = current.getChild(arg);

                /*
                 * If the plugin cannot find a command tree node higher, then the
                 * current floor node "handle" the command
                 */
            else
                parameter++;
    }

    /**
     * @return The command tree node supported to handle the command
     */
    public CommandTreeNode getNode() {
        return current;
    }

    public String[] getArguments() {
        return args;
    }

    /**
     * @return Which parameter the player is currently inputing
     */
    public int extraCount() {
        return parameter;
    }

    public List<String> calculateTabCompletion() {
        return current.calculateTabCompletion(this, Math.max(0, parameter - 1));
    }
}