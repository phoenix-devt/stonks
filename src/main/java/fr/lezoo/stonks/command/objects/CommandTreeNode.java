package fr.lezoo.stonks.command.objects;

import fr.lezoo.stonks.command.objects.parameter.Parameter;
import org.bukkit.command.CommandSender;

import java.util.*;

public abstract class CommandTreeNode {
    private final String id;
    private final CommandTreeNode parent;

    private final Map<String, CommandTreeNode> children = new HashMap<>();
    private final List<Parameter> parameters = new ArrayList<>();

    /**
     * Creates a command tree node which a specific parent and id
     *
     * @param parent The node parent
     * @param id     The node id
     */
    public CommandTreeNode(CommandTreeNode parent, String id) {
        this.id = id;
        this.parent = parent;
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return (hasParent() ? parent.getPath() + " " : "") + getId();
    }

    public Collection<CommandTreeNode> getChildren() {
        return children.values();
    }

    public boolean hasParameters() {
        return !parameters.isEmpty();
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public boolean hasParent() {
        return parent != null;
    }

    public boolean hasChild(String id) {
        return children.containsKey(id.toLowerCase());
    }

    public CommandTreeNode getChild(String id) {
        return children.get(id.toLowerCase());
    }

    public void addChild(CommandTreeNode child) {
        children.put(child.getId(), child);
    }

    public void addParameter(Parameter parameter) {
        parameters.add(parameter);
    }

    public abstract CommandResult execute(CommandSender sender, String[] args);

    public List<String> calculateTabCompletion(CommandTreeExplorer explorer, int parameterIndex) {

        /*
         * Add extra child keys
         */
        List<String> list = new ArrayList<>();
        getChildren().forEach(child -> list.add(child.getId()));

        /*
         * If the player is at the end of a command branch, display the
         * parameter with the right index that the player must input
         */
        if (getParameters().size() > parameterIndex)
            getParameters().get(parameterIndex).autoComplete(explorer, list);

        return list;
    }

    public List<String> calculateUsageList() {
        return calculateUsageList(getPath(), new ArrayList<>());
    }

    /**
     * Recursive method to calculate current usage list
     *
     * @param path   The current tree path explored
     * @param usages List being completed
     * @return The same list with added elements
     */
    private List<String> calculateUsageList(String path, List<String> usages) {

        /*
         * Add to list either if there are parameters or if there are no more
         * children
         */
        if (hasParameters() || getChildren().isEmpty())
            usages.add(path + " " + formatParameters());

        for (CommandTreeNode child : getChildren())
            child.calculateUsageList(path + " " + child.getId(), usages);

        return usages;
    }

    public String formatParameters() {
        StringBuilder str = new StringBuilder();
        for (Parameter param : parameters)
            str.append(param.getKey()).append(" ");
        return (str.length() == 0) ? str.toString() : str.substring(0, str.length() - 1);
    }

    public enum CommandResult {

        /**
         * Command cast successfully, nothing to do
         */
        SUCCESS,

        /**
         * Command cast unsuccessfully, display message handled via command node
         */
        FAILURE,

        /**
         * Send command usage
         */
        THROW_USAGE
    }
}
