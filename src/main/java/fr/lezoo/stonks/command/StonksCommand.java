package fr.lezoo.stonks.command;

import fr.lezoo.stonks.Stonks;
import fr.lezoo.stonks.api.playerdata.PlayerData;
import fr.lezoo.stonks.api.quotation.CreatedQuotation;
import fr.lezoo.stonks.api.quotation.Quotation;
import fr.lezoo.stonks.api.quotation.QuotationInfo;
import fr.lezoo.stonks.api.quotation.QuotationTimeDisplay;
import fr.lezoo.stonks.api.util.message.Message;
import fr.lezoo.stonks.listener.temp.RemoveBoardListener;
import org.bukkit.Bukkit;
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

        if(args.length==0) {
            player.sendMessage(ChatColor.RED+"Authorized commands : givemap adddisplay createquotation removeboard removequotation ");
            return true;
        }

        if(args[0].equalsIgnoreCase("givetradingbook")) {
            player.getInventory().addItem(Stonks.plugin.itemManager.createTradingBook());
        }


        if(args[0].equalsIgnoreCase("removequotation")) {
            if(args.length!=2) {
                player.sendMessage("usage : /stonks removequotation quotationid");
                return true;
            }
            if(!Stonks.plugin.quotationManager.has(args[1].toLowerCase())) {
                player.sendMessage(ChatColor.RED+"There is no quotation with this ID");
                return true;
            }
            Stonks.plugin.quotationManager.remove(args[1].toLowerCase());
            return true;
        }


        if (args[0].equalsIgnoreCase("givemap")) {
            if(args.length!=3) {
                player.sendMessage(ChatColor.RED+"usage : /stonks givemap quotationid time");
                return true;
            }
            if(!Stonks.plugin.quotationManager.has(args[1])){
                player.sendMessage("There is no quotation with this id");
                return true;
            }
            if(!QuotationTimeDisplay.checkQuotationTimeDisplay(args[2])) {
                player.sendMessage("Authorized time display : QUARTERHOUR, HOUR, DAY, WEEK, MONTH, YEAR");
                return true;
            }
            Quotation quotation =Stonks.plugin.quotationManager.get(args[1]);
            QuotationTimeDisplay time = QuotationTimeDisplay.valueOf(args[2]);
            player.getInventory().addItem(quotation.createQuotationMap(time));
        }


        if (args[0].equalsIgnoreCase("createquotation")) {
            if (args.length < 5) {
                player.sendMessage("Usage : /stonks createquotation quotationId quotationName StockName InitialPrice");
                return true;
            }
            args[1] = args[1].toLowerCase();
            args[2] = args[2].toLowerCase();
            args[3] = args[3].toLowerCase();

            if (Stonks.plugin.quotationManager.has(args[1])) {
                player.sendMessage(ChatColor.RED + "There is already a quotation with ID " + args[1]);
                return true;
            }

            double initialPrice;
            try {
                initialPrice = Double.parseDouble(args[4]);
            } catch (IllegalArgumentException e) {
                player.sendMessage(ChatColor.RED + "You didn't enter the initial price correctly");
                return true;
            }
            Stonks.plugin.quotationManager.register(new CreatedQuotation(args[1], args[2], args[3], null, new QuotationInfo(System.currentTimeMillis(), initialPrice)));


        }


        if (args[0].equalsIgnoreCase("removeboard")) {
            if (args.length == 1) {
                player.sendMessage(ChatColor.YELLOW + "You Have 5 seconds to break the block!");
                RemoveBoardListener removeBoardListener = new RemoveBoardListener(player);

                // After 5s the listener is unregistered with HandlerList.unregisterAll(listener)
                Bukkit.getScheduler().scheduleSyncDelayedTask(Stonks.plugin, () -> removeBoardListener.close(), 100L);
                return true;
            } else {
                player.sendMessage(ChatColor.RED + "You didn't use the command properly");
            }
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
                    player.sendMessage(ChatColor.RED + "Could not find a quotation with ID '" + args[2] + "'");
                    return true;
                }


            }

            if (args[1].equalsIgnoreCase("board")) {
                if (args.length != 6) {
                    player.sendMessage(ChatColor.RED + "Usage: /stonks display board <quotation> <quotation-time-display> <width> <height>");
                    return true;
                }

                String quotationId = args[2];
                if (!Stonks.plugin.quotationManager.has(quotationId)) {
                    player.sendMessage(ChatColor.RED + "Could not find a quotation with ID '" + args[2] + "'");
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

                QuotationTimeDisplay time = null;
                args[3] = args[3].toUpperCase();
                if (!QuotationTimeDisplay.checkQuotationTimeDisplay(args[3])) {
                    player.sendMessage(ChatColor.RED + "You can have the time display QUARTERHOUR,HOUR,DAY,WEEK,MONTH,YEAR");
                    return true;
                }
                time = QuotationTimeDisplay.valueOf(args[3]);

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
                quotation.createQuotationBoard(false, location.add(face.getDirection().multiply(2)), face, time, width, height);
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
