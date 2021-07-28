package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.PlayerData;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.util.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class StonksCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command is only for players.");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("stonks.admin")) {
            Message.NOT_ENOUGH_PERMISSIONS.format().send(player);
            return true;
        }

        // Test command
        if (args.length == 0) {


            Quotation quotation = Stonks.plugin.quotationManager.getQuotations().stream().findAny().get();
            player.getInventory().addItem(quotation.createQuotationMap(50));

            return true;
        }

        if (args[0].equalsIgnoreCase("adddisplay")) {
            if (args.length < 2) {
                player.sendMessage("Usage: /stonks adddisplay <board/sign> <quotation> ...");
                return true;
            }

            if (args[1].equalsIgnoreCase("sign")) {
                if (args.length != 3) {
                    player.sendMessage(ChatColor.RED + "Usage: /stonks display sign <quotation>");
                    return true;
                }

                String quotationId = args[2];
                if (!Stonks.plugin.quotationManager.has(quotationId)) {
                    player.sendMessage(ChatColor.RED + "Could not find a quotation with ID '" + args[0] + "'");
                    return true;
                }


            }

            if (args[1].equalsIgnoreCase("board")) {
                if (args.length != 6) {
                    player.sendMessage(ChatColor.RED + "Usage: /stonks display board <quotation> <data-number> <width> <height>");
                    return true;
                }

                String quotationId = args[2];
                if (!Stonks.plugin.quotationManager.has(quotationId)) {
                    player.sendMessage(ChatColor.RED + "Could not find a quotation with ID '" + args[0] + "'");
                    return true;
                }

                // Find the player's direction using max value of scalar product
                final Vector direction = player.getEyeLocation().getDirection();

                BlockFace face = BlockFace.NORTH;
                double val = direction.dot(face.getDirection());

                for (BlockFace checked : new BlockFace[]{BlockFace.WEST, BlockFace.SOUTH, BlockFace.EAST})
                    if (direction.dot(checked.getDirection()) > val) {
                        val = direction.dot(checked.getDirection());
                        face = checked;
                    }

                int numberData = 0;
                try {
                    numberData = Integer.parseInt(args[3]);
                } catch (IllegalArgumentException exception) {
                    player.sendMessage(ChatColor.RED + "Could not parse data number '" + args[3] + "'");
                    return false;
                }

                int width = 0;
                try {
                    width = Integer.parseInt(args[4]);
                } catch (IllegalArgumentException exception) {
                    player.sendMessage(ChatColor.RED + "Could not parse width '" + args[4] + "'");
                    return false;
                }

                int height = 0;
                try {
                    height = Integer.parseInt(args[5]);
                } catch (IllegalArgumentException exception) {
                    player.sendMessage(ChatColor.RED + "Could not parse height '" + args[5] + "'");
                    return false;
                }

                // Work with integers instead to simplify calculations
                Location location = new Location(player.getWorld(), player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
                Quotation quotation = Stonks.plugin.quotationManager.get(quotationId);
                quotation.createQuotationBoard(false, location.add(face.getDirection()), face, numberData, width, height);
                return true;

            }

            if (args[1].equalsIgnoreCase("sign")) {


            }
        }


        if (args[0].equals("shareitem")) {


            // debug
            PlayerData playerData = PlayerData.get(player);
            Stonks.plugin.configManager.QUOTATION_LIST.generate(playerData).open();

            return true;
        }

        return true;
    }
}
