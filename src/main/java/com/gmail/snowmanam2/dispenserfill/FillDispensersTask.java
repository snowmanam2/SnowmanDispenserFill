package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FillDispensersTask implements FillSystemTask {

	private Player player;
	private ItemType item;
	private InventoryGroup group;
	private FillMode mode;
	
	public FillDispensersTask (Player player, ItemType item, InventoryGroup group, FillMode mode) {
		this.player = player;
		this.item = item;
		this.group = group;
		this.mode = mode;
	}
	
	public int getComplexity() {
		return 2;
	}
	
	public void run() {
		if (player == null) {
			return;
		}
		
		InventoryWrapper playerInventory = new InventoryWrapper(player.getInventory());
		int playerQty = playerInventory.getItemAmount(item);
		
		int dispenserQty = group.getSize();
		String itemName = item.getName();
		
		if (dispenserQty == 0) {
			player.sendMessage(ChatColor.RED.toString()+"There are no dispensers nearby to fill.");
			return;
		}
		
		int extra = 0;
		
		switch (mode) {
		case AUTO:
			playerInventory.removeItem(item);
			extra = group.fillAuto(item, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Filled/reorganized "+dispenserQty+" dispensers with "+playerQty+" "+itemName);
			break;
		case ADD:
			playerInventory.removeItem(item);
			extra = group.fillAdd(item, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Added "+playerQty+" total "+itemName+" to "+dispenserQty+" dispensers");
			break;
		case ADDEQUAL:
			playerInventory.removeItem(item);
			extra = group.fillAddEqual(item, playerQty);
			player.sendMessage(ChatColor.GREEN.toString()+"Added "+(playerQty-extra)+" total "+itemName+" to "+dispenserQty+" dispensers");
			break;
		case FILLALL:
			if (player.hasPermission("dispenserfill.fillall")) {
				group.fillAll(item);
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
				int lost = group.fillAdd(item, overflow);
				player.sendMessage(ChatColor.DARK_GRAY.toString()+(overflow-lost)+" "+itemName+" was recycled into available dispensers");
				if (lost > 0) {
					player.sendMessage(ChatColor.RED.toString()+lost+" "+itemName+" was lost!");
				}
			}
		}

	}

}
