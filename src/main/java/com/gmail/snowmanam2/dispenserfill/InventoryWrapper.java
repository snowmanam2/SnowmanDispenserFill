package com.gmail.snowmanam2.dispenserfill;

import java.util.Iterator;
import java.util.Map;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/* Workaround wrapper class for Bukkit's inventory system */

public class InventoryWrapper {
	private Inventory inventory;
	
	public InventoryWrapper (Inventory inv) {
		inventory = inv;
	}
	
	public int getItemAmount (ItemType item) {
		int amount = 0;
		
		for (ItemStack stack : inventory.getContents()) {
			if (stack != null) {
				if (item.isSimilar(new ItemType(stack))) {
					amount += stack.getAmount();
				}
			}
		}
		
		return amount;
	}
	
	public void removeItem (ItemType item) {
		for (ItemStack stack : inventory.getContents()) {
			if (stack != null) {
				if (item.isSimilar(new ItemType(stack))) {
					inventory.remove(stack);
				}
			}
		}
	}
	
	public int addItem (ItemType item, int amount) {
		if (amount == 0) {
			return 0;
		}
		
		ItemStack stack = item.toItemStack(amount);
		
		Map<Integer, ItemStack> remainder = inventory.addItem(stack);
		
		return getStacksQuantity(remainder);
	}
	
	public void fillWithItem (ItemType item) {
		int amount = inventory.getSize() * item.getMaxStackSize();
		
		addItem(item, amount);
	}
	
	public void clear () {
		inventory.clear();
	}
	
	/* getStacksQuantity
	 * Returns the total quantity of a list of stacks, as returned by the Bukkit
	 * Inventory methods.
	 */
	@SuppressWarnings("unchecked")
	private int getStacksQuantity (Map<Integer, ? extends ItemStack> stacks) {
		Iterator<?> itr = stacks.entrySet().iterator();
		
		int qty = 0;
		
		while (itr.hasNext()) {
			Map.Entry<Integer, ItemStack> pair = (Map.Entry<Integer, ItemStack>) itr.next();
			ItemStack is = pair.getValue();
			
			qty += is.getAmount();
		}
		
		return qty;
	}
}
