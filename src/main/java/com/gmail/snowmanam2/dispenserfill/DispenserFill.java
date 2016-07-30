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
			boolean auto = true;
			try {
				if (args.length > 2) {
					if (args[1].equalsIgnoreCase("add")) {
						auto = false;
					}
					
					radius = Integer.parseInt(args[2]);
					getLogger().info(args[1]);
					getLogger().info(args[2]);
				} else if (args.length > 1) {
					radius = Integer.parseInt(args[1]);
					getLogger().info(args[1]);
				}
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED.toString()+"Radius must be a number");
			}
			
			radius = Math.min(radius, config.getInt("maxRadius"));
			
			Player player = (Player) sender;
			PlayerInventory inventory = player.getInventory();
			
			int playerQty = this.getStacksQuantity(inventory.all(Material.TNT));
			
			Location pos = player.getLocation();
			
			World world = pos.getWorld();
			
			int i, j, k;
			int x, y, z;
			
			x = pos.getBlockX();
			y = pos.getBlockY();
			z = pos.getBlockZ();
			
			ArrayList<Inventory> dispensers = new ArrayList<Inventory>();
			
			int dispenserTNTNum = 0;
			
			for (i = -radius+x; i < radius+x; i++) {
				for (j = -radius+y; j < radius+y; j++) {
					
					if (j > 255 || j < 0) continue;
					
					for (k = -radius+z; k < radius+z; k++) {
						Block b = world.getBlockAt(i, j, k);						
						if (b.getType() == Material.DISPENSER) {
							Dispenser d = (Dispenser) b.getState();
							Inventory dinv = d.getInventory();
							dispenserTNTNum += this.getStacksQuantity(dinv.all(Material.TNT));
							dispensers.add(dinv);
							
						}
					}
				}
			}
			
			int dispenserNumber = dispensers.size();
			int totalQty = dispenserTNTNum + playerQty;
			int qtyEach = totalQty / dispenserNumber;
			int leftoverQty = totalQty % dispenserNumber;
			
			
			if (totalQty < dispenserNumber) {
				
				Iterator<?> itr = dispensers.iterator();
				
				for (int n = 0; n < totalQty; n++) {
					Inventory dinv = (Inventory) itr.next();
					
					leftoverQty += this.addItemsToInventory(dinv, Material.TNT, 1);
				}
				
				this.addItemsToInventory(inventory, Material.TNT, leftoverQty);
				
				sender.sendMessage(ChatColor.YELLOW.toString()+"Partially filled "+totalQty+" dispensers with "+playerQty+" TNT");
			} else if (auto) {	
				inventory.remove(Material.TNT);
				
				for (Inventory dinv : dispensers) {
					dinv.remove(Material.TNT);
					
					leftoverQty += this.addItemsToInventory(dinv, Material.TNT, qtyEach);
				}
				
				this.addItemsToInventory(inventory, Material.TNT, leftoverQty);
				if (playerQty > 0) {
					sender.sendMessage(ChatColor.GREEN.toString()+"Filled and reorganized " + dispenserNumber+" dispensers with "+playerQty+" TNT.");
				} else {
					sender.sendMessage(ChatColor.GREEN.toString()+"Reorganized "+ dispenserNumber+" dispensers");
				}
				
				if (leftoverQty > 0) {
					sender.sendMessage(ChatColor.GREEN.toString()+"Returned "+leftoverQty+" TNT");
				}
			} else {
				inventory.remove(Material.TNT);
				
				qtyEach = playerQty / dispenserNumber;
				leftoverQty = playerQty % dispenserNumber;
				
				for (Inventory dinv : dispensers) {
					leftoverQty += this.addItemsToInventory(dinv, Material.TNT, qtyEach);
				}
				
				this.addItemsToInventory(inventory, Material.TNT, leftoverQty);
				
				sender.sendMessage(ChatColor.GREEN.toString()+"Added " + qtyEach +" TNT to "+dispenserNumber+" dispensers");
				sender.sendMessage(ChatColor.GREEN.toString()+"Returned "+leftoverQty+" TNT");
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
	
}
