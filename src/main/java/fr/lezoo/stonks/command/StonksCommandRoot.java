package fr.lezoo.stonks.command;

import fr.lezoo.stonks.command.nodes.GiveMapTreeNode;
import fr.lezoo.stonks.command.nodes.GiveTradingBookTreeNode;
import fr.lezoo.stonks.command.nodes.quotation.QuotationTreeNode;
import fr.lezoo.stonks.command.nodes.ReloadTreeNode;
import fr.lezoo.stonks.command.nodes.display.DisplayTreeNode;
import fr.lezoo.stonks.command.objects.CommandTreeRoot;

public class StonksCommandRoot extends CommandTreeRoot {
    public StonksCommandRoot() {
        super("stonks", "stonks.admin");

        addChild(new GiveMapTreeNode(this));
        addChild(new GiveTradingBookTreeNode(this));
        addChild(new QuotationTreeNode(this));
        addChild(new ReloadTreeNode(this));
        addChild(new DisplayTreeNode(this));
        // addChild(new ShareItemTreeNode(this));
    }
}
