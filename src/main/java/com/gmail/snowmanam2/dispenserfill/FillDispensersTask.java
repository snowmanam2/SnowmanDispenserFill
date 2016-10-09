package com.gmail.snowmanam2.dispenserfill;

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
	
	public long getComplexity() {
		
		/* Rough estimate based on the inventory handling */
		return 40 * group.getSize();
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
			player.sendMessage(Messages.get("fill.noDispensersNearby"));
			return;
		}
		
		int extra = 0;
		
		switch (mode) {
		case AUTO:
			playerInventory.removeItem(item);
			extra = group.fillAuto(item, playerQty);
			player.sendMessage(Messages.get("fill.fillAuto", dispenserQty, playerQty, itemName));
			break;
		case ADD:
			playerInventory.removeItem(item);
			extra = group.fillAdd(item, playerQty);
			player.sendMessage(Messages.get("fill.fillAdd", dispenserQty, playerQty, itemName));
			break;
		case ADDEQUAL:
			playerInventory.removeItem(item);
			extra = group.fillAddEqual(item, playerQty);
			player.sendMessage(Messages.get("fill.fillAddEqual", dispenserQty, playerQty, itemName));
			break;
		case FILLALL:
			if (player.hasPermission("dispenserfill.fillall")) {
				group.fillAll(item);
				player.sendMessage(Messages.get("fill.fillAll", dispenserQty, itemName));
			} else {
				player.sendMessage(Messages.get("fill.noPermission"));
				return;
			}
			break;
			
		}
		
		if (extra > 0) {
			int overflow = playerInventory.addItem(item, extra);
			player.sendMessage(Messages.get("fill.returnMessage", (extra-overflow), itemName));
			
			if(overflow > 0) {
				int lost = group.fillAdd(item, overflow);
				player.sendMessage(Messages.get("fill.recycleMessage", (overflow-lost), itemName));
				if (lost > 0) {
					player.sendMessage(Messages.get("fill.lostMessage", lost, itemName));
				}
			}
		}

	}

}
