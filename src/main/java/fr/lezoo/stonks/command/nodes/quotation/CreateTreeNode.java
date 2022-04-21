package fr.lezoo.stonks.command.nodes.quotation;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.command.objects.CommandTreeNode;
import fr.lezoo.stonks.command.objects.parameter.NumericalParameter;
import fr.lezoo.stonks.command.objects.parameter.SimpleParameter;
import fr.lezoo.stonks.quotation.Quotation;
import fr.lezoo.stonks.quotation.QuotationInfo;
import fr.lezoo.stonks.quotation.handler.FictiveStockHandler;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

public class CreateTreeNode extends CommandTreeNode {
    public CreateTreeNode(CommandTreeNode parent) {
        super(parent, "create");

        addParameter(new SimpleParameter("quotation_id"));
        addParameter(new SimpleParameter("Quotation_Name"));
        addParameter(new NumericalParameter("<initialPrice>", 10, 1000, 1000000));
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        if (args.length < 5)
            return CommandResult.THROW_USAGE;

        args[2] = args[2].toLowerCase();
        args[3] = args[3].toLowerCase().replace("_", " "); // Replace "_" by spaces for the name

        if (Stonks.plugin.quotationManager.has(args[2])) {
            sender.sendMessage(ChatColor.RED + "There is already a quotation with ID " + args[2]);
            return CommandResult.FAILURE;
        }

        double initialPrice;
        try {
            initialPrice = Double.parseDouble(args[4]);
        } catch (IllegalArgumentException exception) {
            sender.sendMessage(ChatColor.RED + "Please enter a valid number");
            return CommandResult.FAILURE;
        }

        Stonks.plugin.quotationManager.register(new Quotation(args[2], args[3], FictiveStockHandler::new, null, null, new QuotationInfo(System.currentTimeMillis(), initialPrice)));
        return CommandResult.SUCCESS;
    }






}
