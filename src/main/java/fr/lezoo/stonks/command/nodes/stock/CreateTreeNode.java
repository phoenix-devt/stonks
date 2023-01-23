package fr.lezoo.stonks.command.nodes.stock;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.NumericalParameter;
import fr.lezoo.stonks.command.objects.parameter.SimpleParameter;
import fr.lezoo.stonks.stock.Stock;
import fr.lezoo.stonks.stock.StockInfo;
import fr.lezoo.stonks.stock.handler.FictiveStockHandler;
import fr.lezoo.stonks.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CreateTreeNode extends CommandTreeNode {
    public CreateTreeNode(CommandTreeNode parent) {
        super(parent, "create");

        addParameter(new SimpleParameter("stock_id"));
        addParameter(new SimpleParameter("StockName"));
        addParameter(new NumericalParameter("<initialPrice>", 10, 1000, 1000000));
        addParameter(new NumericalParameter("<initialSupply>", 1000, 10000, 100000));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 6)
            return CommandResult.THROW_USAGE;

        args[2] = args[2].toLowerCase();
        args[3] = args[3].toLowerCase().replace("_", " "); // Replace "_" by spaces for the name

        if (Stonks.plugin.stockManager.has(args[2])) {
            sender.sendMessage(ChatColor.RED + "There is already a stock with ID " + args[2]);
            return CommandResult.FAILURE;
        }

        double initialPrice, initialSupply;
        try {
            initialPrice = Double.parseDouble(args[4]);
            initialSupply = Double.parseDouble(args[5]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid number");
            return CommandResult.FAILURE;
        }

        Stonks.plugin.stockManager.register(new Stock(args[2], args[3], stock -> new FictiveStockHandler(stock, initialPrice, initialSupply), null, null, new StockInfo(System.currentTimeMillis(), initialPrice)));
        Message.STOCK_CREATED.format("stock-name",args[3]).send(sender);
        return CommandResult.SUCCESS;
    }
}
