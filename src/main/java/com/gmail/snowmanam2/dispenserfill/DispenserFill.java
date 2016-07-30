package com.gmail.snowmanam2.dispenserfill;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.bukkit.inventory.PlayerInventory;
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
			
			int radius = config.getInt("maxRadius");
			FillMode fillMode = FillMode.AUTO;
			
			if (args.length >= 1) {
				String mode = args[0].toUpperCase();
				
				try {
					fillMode = FillMode.valueOf(mode);
				} catch (IllegalArgumentException e) {
					sender.sendMessage(ChatColor.RED.toString()+"Unrecognized fill mode "+mode);
					
					return true;
				}
			}
			if (args.length >= 2) {
				try {
					radius = Math.abs(Integer.parseInt(args[1]));
				} catch (NumberFormatException e) {
					sender.sendMessage(ChatColor.RED.toString()+"Radius must be a number");
					
					return true;
				}
				
			}
			
			
			
			radius = Math.min(radius, config.getInt("maxRadius"));
			
			Player player = (Player) sender;
			Inventory playerInventory = player.getInventory();
			int playerQty = getStacksQuantity(playerInventory.all(Material.TNT));
			
			ArrayList<Inventory> dispensers = getNearbyDispensers(player, radius);
			int dispenserQty = dispensers.size();
			
			int extra = 0;
			
			switch (fillMode) {
			case AUTO:
				playerInventory.remove(Material.TNT);
				extra = fillInventoriesAuto (dispensers, Material.TNT, playerQty);
				sender.sendMessage(ChatColor.GREEN.toString()+"Filled/reorganized "+dispenserQty+" dispensers with "+playerQty+" TNT");
				break;
			case ADD:
				playerInventory.remove(Material.TNT);
				extra = fillInventoriesAdd (dispensers, Material.TNT, playerQty);
				sender.sendMessage(ChatColor.GREEN.toString()+"Added "+playerQty+" total TNT to "+dispenserQty+" dispensers");
				break;
			case ADDEQUAL:
				playerInventory.remove(Material.TNT);
				extra = fillInventoriesAddEqual (dispensers, Material.TNT, playerQty);
				sender.sendMessage(ChatColor.GREEN.toString()+"Added "+(playerQty-extra)+" total TNT to "+dispenserQty+" dispensers");
				break;
			case FILLALL:
				if (player.hasPermission("dispenserfill.fillall")) {
					fillInventoriesFillAll (dispensers, Material.TNT);
					sender.sendMessage(ChatColor.GREEN.toString()+"Filled "+dispenserQty+" dispensers with TNT");
				} else {
					sender.sendMessage(ChatColor.RED.toString()+"You don't have permission to use that mode");
					return true;
				}
				break;
				
			}
			
			if (extra > 0) {
				addItemsToInventory(playerInventory, Material.TNT, extra);
				sender.sendMessage(ChatColor.GREEN.toString()+"Returned "+extra+" TNT");
			}
			
			return true;
		}
		
		return false;
	}
	
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
	
	private int addItemsToInventory (Inventory inv, Material mat, int quantity) {

		if (quantity == 0) return 0;
		
		HashMap<Integer, ItemStack> leftover = inv.addItem(new ItemStack(mat, quantity));
		
		return this.getStacksQuantity(leftover);
	}
	
	private ArrayList<Inventory> getNearbyDispensers (Player p, int radius) {
		Location pos = p.getLocation();
		
		World world = pos.getWorld();
		
		int i, j, k;
		int x, y, z;
		
		x = pos.getBlockX();
		y = pos.getBlockY();
		z = pos.getBlockZ();
		
		ArrayList<Inventory> dispensers = new ArrayList<Inventory>();
		
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
	
	private int getInventoriesQty (ArrayList<Inventory> invs, Material mat) {
		int qty = 0;
		
		for (Inventory inv : invs) {
			qty += getStacksQuantity(inv.all(mat));
		}
		
		return qty;
	}
	
	private int fillInventoriesAuto (ArrayList<Inventory> invs, Material mat, int qty) {
		int totalQty = qty + getInventoriesQty(invs, mat);
		
		int qtyEach = totalQty / invs.size();
		int remainder = totalQty % invs.size();
		
		for (Inventory inv : invs) {
			inv.remove(mat);
			remainder += addItemsToInventory (inv, mat, qtyEach);
		}
		
		return remainder;
	}
	
	private int fillInventoriesAddEqual (ArrayList<Inventory> invs, Material mat, int qty) {
		int qtyEach = qty / invs.size();
		int remainder = qty % invs.size();
		
		for (Inventory inv : invs) {
			remainder += addItemsToInventory (inv, mat, qtyEach);
		}
		
		return remainder;
	}
	
	private int fillInventoriesAdd (ArrayList<Inventory> invs, Material mat, int qty) {
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
	
	private void fillInventoriesFillAll (ArrayList<Inventory> invs, Material mat) {
		int stackSize = mat.getMaxStackSize();
		
		for (Inventory inv : invs) {
			addItemsToInventory (inv, mat, stackSize*inv.getSize());
		}
	}
	
}
