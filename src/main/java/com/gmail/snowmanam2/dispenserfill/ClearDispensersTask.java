package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.entity.Player;

public class ClearDispensersTask implements FillSystemTask {
	private Player player;
	private InventoryGroup group;
	
	public ClearDispensersTask (Player player, InventoryGroup group) {
		this.player = player;
		this.group = group;
	}
	
	public void run() {
		group.clearAll();
		player.sendMessage(Messages.get("clear.clearMessage", group.getSize()));
	}

	public long getComplexity() {
		return 1;
	}

}
