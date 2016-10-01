package com.gmail.snowmanam2.dispenserfill;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;

public class FillSystem {
	private Queue<FillSystemTask> taskQueue;
	private boolean fullChunkHeight;
	
	public FillSystem (DispenserFill plugin) {
		taskQueue = new LinkedList<FillSystemTask>();
		BukkitScheduler scheduler = plugin.getServer().getScheduler();
		final int complexityPerCycle = plugin.getConfig().getInt("complexityPerCycle");
		final long taskTickInterval = plugin.getConfig().getInt("taskTickInterval");
		this.fullChunkHeight = plugin.getConfig().getBoolean("fullChunkHeight");
		
		scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            public void run() {
            	int operationsPerformed = 0;
            	FillSystemTask task;
            	
                while ((task = taskQueue.peek()) != null) {
                		operationsPerformed += task.getComplexity();
                		if (operationsPerformed <= complexityPerCycle) {
                			task.run();
                			taskQueue.remove();
                		} else {
                			break;
                		}
                }
            }
        }, 0L, taskTickInterval);
	}
	
	public void fillDispensers (Player p, ItemType item, FillMode mode, int radius) {
		
		/* Group to be later populated with dispensers */
		InventoryGroup group = new InventoryGroup();
		
		findNearbyDispensers(p, group, radius);
		
		/* Note filling task is queued last because it depends on the InventoryGroup being populated */
		FillSystemTask fillTask = new FillDispensersTask(p, item, group, mode);
		taskQueue.add(fillTask);
	}
	
	public void clearDispensers (Player p, int radius) {
		
		/* Group to be later populated with dispensers */
		InventoryGroup group = new InventoryGroup();
		
		findNearbyDispensers(p, group, radius);
		
		/* Note clearing task is queued last because it depends on the InventoryGroup being populated */
		FillSystemTask clearTask = new ClearDispensersTask(p, group);
		taskQueue.add(clearTask);
	}
	
	private void findNearbyDispensers(Player p, InventoryGroup group, int radius) {
		Location playerLocation = p.getLocation();
		Location limit1 = new Location(p.getWorld(), playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
		limit1.add(-radius, -radius, -radius);
		Location limit2 = new Location(p.getWorld(), playerLocation.getX(), playerLocation.getY(), playerLocation.getZ());
		limit2.add(radius, radius, radius);
		
		if(this.fullChunkHeight) {
			limit1.setY(0);
			limit2.setY(255);
		}
		
		World world = p.getWorld();
		
		/* Chunk coordinate conversion */
		int minx = limit1.getBlockX() >> 4;
		int maxx = limit2.getBlockX() >> 4;
		int minz = limit1.getBlockZ() >> 4;
		int maxz = limit2.getBlockZ() >> 4;
		
		int i, j;
		
		for (i = minx; i <= maxx; i++) {
			for (j = minz; j <= maxz; j++) {
				FillSystemTask scanTask = new ScanChunkTask(group, world.getChunkAt(i, j), limit1, limit2);
				taskQueue.add(scanTask);
			}
		}
	}
}
