package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class DispenserFill extends JavaPlugin {
	private FileConfiguration config = getConfig();
	
	@Override
	public void onEnable() {
		config.addDefault("maxRadius", 50);
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("tntfill")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}
			
			ItemType item = new ItemType(Material.TNT);
			item.setName("TNT");
			
			fillDispensers ((Player)sender, item, args);
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("dispenserfill")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}
			
			Player p = (Player) sender;
			
			ItemStack stack = p.getItemInHand();
			ItemType item = new ItemType(stack);
			
			if (item.getMaterial().equals(Material.AIR)) {
				sender.sendMessage(ChatColor.RED.toString()+"Put an item in your hand");
				return true;
			}
			
			fillDispensers (p, item, args);
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("unfillall")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}
			
			
			Player player = (Player) sender;
			int radius = config.getInt("maxRadius");
			
			InventoryGroup dispensers = getNearbyDispensers(player, radius);
			dispensers.clearAll();
			
			sender.sendMessage(ChatColor.GREEN.toString()+"Cleared "+dispensers.getSize()+" dispensers");
			
			return true;
		}
		
			
		
		return false;
	}
	
	private void fillDispensers (Player player, ItemType item, String[] args) {
		int radius = config.getInt("maxRadius");
		FillMode fillMode = FillMode.AUTO;
		
		String itemName = item.getName();
		
		if (args.length >= 1) {
			String mode = args[0].toUpperCase();
			
			try {
				fillMode = FillMode.valueOf(mode);
			} catch (IllegalArgumentException e) {
				player.sendMessage(ChatColor.RED.toString()+"Unrecognized fill mode "+mode);
				
				return;
			}
		}
		if (args.length >= 2) {
			try {
				radius = Math.abs(Integer.parseInt(args[1]));
			} catch (NumberFormatException e) {
				player.sendMessage(ChatColor.RED.toString()+"Radius must be a number");
				
				return;
			}
		}
		
		
		if (!player.hasPermission("dispenserfill.overrideradius")) {
			radius = Math.min(radius, config.getInt("maxRadius"));
		}
		
		InventoryWrapper playerInventory = new InventoryWrapper(player.getInventory());
		int playerQty = playerInventory.getItemAmount(item);
		
		InventoryGroup dispensers = getNearbyDispensers(player, radius);
		int dispenserQty = dispensers.getSize();
		
		int extra = 0;
		
		switch (fillMode) {
		case AUTO:
			playerInventory.removeItem(item);
			extra = dispensers.fillAuto(item, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Filled/reorganized "+dispenserQty+" dispensers with "+playerQty+" "+itemName);
			break;
		case ADD:
			playerInventory.removeItem(item);
			extra = dispensers.fillAdd(item, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Added "+playerQty+" total "+itemName+" to "+dispenserQty+" dispensers");
			break;
		case ADDEQUAL:
			playerInventory.removeItem(item);
			extra = dispensers.fillAddEqual(item, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Added "+(playerQty-extra)+" total "+itemName+" to "+dispenserQty+" dispensers");
			break;
		case FILLALL:
			if (player.hasPermission("dispenserfill.fillall")) {
				dispensers.fillAll(item);
				player.sendMessage(ChatColor.GREEN.toString()+"Filled "+dispenserQty+" dispensers with "+itemName);
			} else {
				player.sendMessage(ChatColor.RED.toString()+"You don't have permission to use that mode");
				return;
			}
			break;
			
		}
		
		if (extra > 0) {
			int overflow = playerInventory.addItem(item, extra);
			player.sendMessage(ChatColor.GREEN.toString()+"Returned "+(extra-overflow)+" "+itemName);
			
			if(overflow > 0) {
				int lost = dispensers.fillAdd(item, overflow);
				player.sendMessage(ChatColor.DARK_GRAY.toString()+(overflow-lost)+" "+itemName+" was recycled into available dispensers");
				if (lost > 0) {
					player.sendMessage(ChatColor.RED.toString()+lost+" "+itemName+" was lost!");
				}
			}
		}
		
		return;
	}

	private InventoryGroup getNearbyDispensers (Player p, int radius) {
		InventoryGroup result = new InventoryGroup();
		
		Location pos = p.getLocation();
		
		World world = pos.getWorld();
		
		int i, j, k;
		int x, y, z;
		
		x = pos.getBlockX();
		y = pos.getBlockY();
		z = pos.getBlockZ();
		
		for (i = -radius+x; i < radius+x; i++) {
			for (j = -radius+y; j < radius+y; j++) {
				
				if (j > 255 || j < 0) continue;
				
				for (k = -radius+z; k < radius+z; k++) {
					Block b = world.getBlockAt(i, j, k);						
					if (b.getType() == Material.DISPENSER) {
						Dispenser d = (Dispenser) b.getState();
						Inventory dinv = d.getInventory();
						result.addInventory(new InventoryWrapper(dinv));
					}
				}
			}
		}
		
		return result;
	}
	
}
