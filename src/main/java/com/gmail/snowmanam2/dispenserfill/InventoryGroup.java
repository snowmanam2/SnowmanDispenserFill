package com.gmail.snowmanam2.dispenserfill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class InventoryGroup {
	List<InventoryWrapper> inventories;
	
	public InventoryGroup () {
		inventories = new ArrayList<InventoryWrapper>();
	}
	
	public void addInventory (InventoryWrapper inv) {
		inventories.add(inv);
	}
	
	public int getItemQuantity (ItemType item) {
		int amount = 0;
		
		for (InventoryWrapper inv : inventories) {
			amount += inv.getItemAmount(item);
		}
		
		return amount;
	}
	
	public int getSize () {
		return inventories.size();
	}
	
	public void clearItems (ItemType item) {
		for (InventoryWrapper inv : inventories) {
			inv.removeItem(item);
		}
	}
	
	public void clearAll () {
		for (InventoryWrapper inv : inventories) {
			inv.clear();
		}
	}
	
	/* addItemsEqually
	 * Spreads the quantity of item equally in all inventories.
	 * Returns the quantity of item that is uneven or doesn't
	 * fit in the inventories.
	 */
	public int addItemsEqually (ItemType item, int qty) {
		if (inventories.size() == 0) {
			return qty;
		}
		int remainder = qty % inventories.size();
		
		int amountEach = qty / inventories.size();
		
		for (InventoryWrapper inv : inventories) {
			remainder += inv.addItem(item, amountEach);
		}
		
		return remainder;
	}
	
	
	/* addItemsUnequally
	 * Attempt to completely use all available quantity, even if
	 * it means the finished filling won't be equal.
	 * Returns the quantity of item that couldn't be added.
	 */
	public int addItemsUnequally (ItemType item, int qty) {
		if (inventories.size() == 0) {
			return qty;
		}
		
		int remainder = qty;
		
		List<InventoryWrapper> temp = new LinkedList<InventoryWrapper> (inventories);
		
		while (remainder > 0 && temp.size() > 0) {
			Iterator<InventoryWrapper> itr = temp.listIterator();
			while (itr.hasNext() && remainder > 0) {
				InventoryWrapper inv = itr.next();
				
				int extra = inv.addItem(item, 1);
				remainder--;
				
				if (extra > 0) {
					itr.remove();
				}
				
				remainder += extra;
			}
		}
		
		return remainder;
	}
	
	/* Auto Mode:
	 * Attempts to fill all inventories with the same amount of the item.
	 * Excess is returned.
	 */
	public int fillAuto (ItemType item, int qty) {
		
		int totalQty = qty + getItemQuantity(item);
		
		clearItems(item);
		int remainder = addItemsEqually(item, totalQty);
		
		return remainder;
	}
	
	
	/* AddEqual Mode:
	 * Attempts to add all dispensers with an equal amount of the item without
	 * accounting for the existing quantity. Excess is returned.
	 */
	public int fillAddEqual (ItemType item, int qty) {
		
		int remainder = addItemsEqually(item, qty);
		
		return remainder;
	}
	
	/* Add Mode:
	 * First attempts to add all items as with the AddEqual mode, but then adds all possible
	 * excess to the inventories. Excess is only returned if no inventories are available.
	 * Note that fillInventoriesEqually is called first to improve performance.
	 */
	public int fillAdd (ItemType item, int qty) {
		
		int remainder = fillAddEqual(item, qty);
		
		remainder = addItemsUnequally(item, remainder);
		
		return remainder;
	}
	
	/* FillAll Mode
	 * Fills all inventories with the maximum amount of the item. Note that this method
	 * does NOT check for available quantities and is therefore intended only for
	 * OPs and creative mode.
	 */
	public void fillAll (ItemType item) {
		for (InventoryWrapper inv : inventories) {
			inv.fillWithItem (item);
		}
	}
}
