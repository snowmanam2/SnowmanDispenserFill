package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.inventory.Inventory;

public class ScanChunkTask implements FillSystemTask {
	private InventoryGroup group;
	private Chunk chunk;
	private Location limit1;
	private Location limit2;
	private int minx;
	private int miny;
	private int minz;
	private int maxx;
	private int maxy;
	private int maxz;
	
	public ScanChunkTask (InventoryGroup group, Chunk chunk, Location limit1, Location limit2) {
		this.group = group;
		this.chunk = chunk;
		
		if (this.chunk == null) {
			return;
		}
		
		int x = this.chunk.getX() << 4;
		int z = this.chunk.getZ() << 4;
		
		minx = Math.min(limit1.getBlockX()-x, limit2.getBlockX()-x);
		minx = Math.max(minx, 0);
		
		minz = Math.min(limit1.getBlockZ()-z, limit2.getBlockZ()-z);
		minz = Math.max(minz, 0);
		
		miny = Math.min(limit1.getBlockY(), limit2.getBlockY());
		miny = Math.max(miny, 0);
		
		maxx = Math.max(limit1.getBlockX()-x, limit2.getBlockX()-x);
		maxx = Math.min(maxx, 15);
		
		maxz = Math.max(limit1.getBlockZ()-z, limit2.getBlockZ()-z);
		maxz = Math.min(maxz, 15);
		
		maxy = Math.max(limit1.getBlockY(), limit2.getBlockY());
		maxy = Math.min(maxy, 255);
	}
	
	public long getComplexity() {
		return (maxx-minx)*(maxy-miny)*(maxz-minz);
	}
	
	public void run () {
		
		
		int i, j, k;
		
		for (i = minx; i <= maxx; i++) {
			for (j = miny; j <= maxy; j++) {
				for (k = minz; k <= maxz; k++) {
					Block b = chunk.getBlock(i, j, k);						
					if (b.getType() == Material.DISPENSER) {
						Dispenser d = (Dispenser) b.getState();
						Inventory dinv = d.getInventory();
						group.addInventory(new InventoryWrapper(dinv));
					}
				}
			}
		}
	}
}
