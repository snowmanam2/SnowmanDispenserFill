package com.gmail.snowmanam2.dispenserfill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
			
			fillDispensers ((Player)sender, Material.TNT, args);
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("dispenserfill")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}
			
			Player p = (Player) sender;
			
			ItemStack stack = p.getItemInHand();
			Material mat = stack.getType();
			
			if (mat.equals(Material.AIR)) {
				sender.sendMessage(ChatColor.RED.toString()+"Put an item in your hand");
				return true;
			}
			
			fillDispensers (p, mat, args);
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("unfillall")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage("This command can only be run by a player.");
				return true;
			}
			
			
			Player player = (Player) sender;
			int radius = config.getInt("maxRadius");
			
			List<Inventory> dispensers = getNearbyDispensers(player, radius);
			clearInventories(dispensers);
			
			sender.sendMessage(ChatColor.GREEN.toString()+"Cleared "+dispensers.size()+" dispensers");
			
			return true;
		}
		
			
		
		return false;
	}
	
	private void fillDispensers (Player player, Material mat, String[] args) {
		int radius = config.getInt("maxRadius");
		FillMode fillMode = FillMode.AUTO;
		
		String itemName = mat.toString().toLowerCase().replaceAll("_", " ");
		
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
		
		Inventory playerInventory = player.getInventory();
		int playerQty = getStacksQuantity(playerInventory.all(mat));
		
		List<Inventory> dispensers = getNearbyDispensers(player, radius);
		int dispenserQty = dispensers.size();
		
		int extra = 0;
		
		switch (fillMode) {
		case AUTO:
			playerInventory.remove(mat);
			extra = fillInventoriesAuto (dispensers, mat, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Filled/reorganized "+dispenserQty+" dispensers with "+playerQty+" "+itemName);
			break;
		case ADD:
			playerInventory.remove(mat);
			extra = fillInventoriesAdd (dispensers, mat, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Added "+playerQty+" total "+itemName+" to "+dispenserQty+" dispensers");
			break;
		case ADDEQUAL:
			playerInventory.remove(mat);
			extra = fillInventoriesAddEqual (dispensers, mat, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Added "+(playerQty-extra)+" total "+itemName+" to "+dispenserQty+" dispensers");
			break;
		case FILLALL:
			if (player.hasPermission("dispenserfill.fillall")) {
				fillInventoriesFillAll (dispensers, mat);
				player.sendMessage(ChatColor.GREEN.toString()+"Filled "+dispenserQty+" dispensers with "+itemName);
			} else {
				player.sendMessage(ChatColor.RED.toString()+"You don't have permission to use that mode");
				return;
			}
			break;
			
		}
		
		if (extra > 0) {
			int overflow = addItemsToInventory(playerInventory, mat, extra);
			player.sendMessage(ChatColor.GREEN.toString()+"Returned "+(extra-overflow)+" "+itemName);
			
			if(overflow > 0) {
				int lost = fillInventoriesAdd(dispensers, mat, overflow);
				player.sendMessage(ChatColor.DARK_GRAY.toString()+(overflow-lost)+" "+itemName+" was recycled into available dispensers");
				if (lost > 0) {
					player.sendMessage(ChatColor.RED.toString()+lost+" "+itemName+" was lost!");
				}
			}
		}
		
		return;
	}
	
	/* getStacksQuantity
	 * Returns the total quantity of a list of stacks, as returned by the Bukkit
	 * Inventory methods.
	 */
	@SuppressWarnings("unchecked")
	private int getStacksQuantity (HashMap<Integer, ? extends ItemStack> stacks) {
		Iterator<?> itr = stacks.entrySet().iterator();
		
		int qty = 0;
		
		while (itr.hasNext()) {
			Map.Entry<Integer, ItemStack> pair = (Map.Entry<Integer, ItemStack>) itr.next();
			ItemStack is = pair.getValue();
			
			qty += is.getAmount();
		}
		
		return qty;
	}
	
	/* addItemsToInventory
	 * Wrapper function to attempt to add a quantity of items to an inventory,
	 * while accounting for zero quantities and excess.
	 */
	private int addItemsToInventory (Inventory inv, Material mat, int quantity) {

		if (quantity == 0) return 0;
		
		HashMap<Integer, ItemStack> leftover = inv.addItem(new ItemStack(mat, quantity));
		
		return this.getStacksQuantity(leftover);
	}
	
	private List<Inventory> getNearbyDispensers (Player p, int radius) {
		Location pos = p.getLocation();
		
		World world = pos.getWorld();
		
		int i, j, k;
		int x, y, z;
		
		x = pos.getBlockX();
		y = pos.getBlockY();
		z = pos.getBlockZ();
		
		List<Inventory> dispensers = new ArrayList<Inventory>();
		
		for (i = -radius+x; i < radius+x; i++) {
			for (j = -radius+y; j < radius+y; j++) {
				
				if (j > 255 || j < 0) continue;
				
				for (k = -radius+z; k < radius+z; k++) {
					Block b = world.getBlockAt(i, j, k);						
					if (b.getType() == Material.DISPENSER) {
						Dispenser d = (Dispenser) b.getState();
						Inventory dinv = d.getInventory();
						dispensers.add(dinv);
						
					}
				}
			}
		}
		
		return dispensers;
	}
	
	/* getInventoriesQty
	 * Returns the total amount of a given item in a list of inventories.
	 */
	private int getInventoriesQty (List<Inventory> invs, Material mat) {
		int qty = 0;
		
		for (Inventory inv : invs) {
			qty += getStacksQuantity(inv.all(mat));
		}
		
		return qty;
	}
	
	
	/* Auto Mode:
	 * Attempts to fill all inventories with the same amount of the item.
	 * Excess is returned.
	 */
	private int fillInventoriesAuto (List<Inventory> invs, Material mat, int qty) {
		if (invs.size() == 0) {
			return qty;
		}
		
		int totalQty = qty + getInventoriesQty(invs, mat);
		
		int qtyEach = totalQty / invs.size();
		int remainder = totalQty % invs.size();
		
		for (Inventory inv : invs) {
			inv.remove(mat);
			remainder += addItemsToInventory (inv, mat, qtyEach);
		}
		
		return remainder;
	}
	
	
	/* AddEqual Mode:
	 * Attempts to add all dispensers with an equal amount of the item without
	 * accounting for the existing quantity. Excess is returned.
	 */
	private int fillInventoriesAddEqual (List<Inventory> invs, Material mat, int qty) {
		if (invs.size() == 0) {
			return qty;
		}
		
		int qtyEach = qty / invs.size();
		int remainder = qty % invs.size();
		
		for (Inventory inv : invs) {
			remainder += addItemsToInventory (inv, mat, qtyEach);
		}
		
		return remainder;
	}
	
	/* Add Mode:
	 * First attempts to add all items as with the AddEqual mode, but then adds all possible
	 * excess to the inventories. Excess is only returned if no inventories are available.
	 */
	private int fillInventoriesAdd (List<Inventory> invs, Material mat, int qty) {
		if (invs.size() == 0) {
			return qty;
		}
		
		int remainder = fillInventoriesAddEqual(invs, mat, qty);
		
		while (remainder > 0 && invs.size() > 0) {
			Iterator<Inventory> itr = invs.listIterator();
			while (itr.hasNext() && remainder > 0) {
				Inventory inv = itr.next();
				
				int extra = addItemsToInventory(inv, mat, 1);
				remainder--;
				
				if (extra > 0) {
					itr.remove();
				}
				
				remainder += extra;
			}
		}
		
		return remainder;
	}
	
	/* FillAll Mode
	 * Fills all inventories with the maximum amount of the item. Note that this method
	 * does NOT check for available quantities and is therefore intended only for
	 * OPs and creative mode.
	 */
	private void fillInventoriesFillAll (List<Inventory> invs, Material mat) {
		int stackSize = mat.getMaxStackSize();
		
		for (Inventory inv : invs) {
			addItemsToInventory (inv, mat, stackSize*inv.getSize());
		}
	}
	
	/* clearInventories
	 * Helper function to clear all inventories. This method wipes the inventories without
	 * recycling their contents, so it is only intended for OPs and creative mode.
	 */
	private void clearInventories (List<Inventory> invs) {
		for (Inventory inv : invs) {
			inv.clear();
		}
	}
	
}
