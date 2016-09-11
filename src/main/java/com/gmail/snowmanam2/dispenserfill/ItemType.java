package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class ItemType {
	private ItemStack item;
	String name;
	
	public ItemType (Material m) {
		item = new ItemStack(m);
		name = getDefaultName();
	}
	
	public ItemType (Material m, short durability) {
		item = new ItemStack(m);
		item.setDurability(durability);
		name = getDefaultName();
	}
	
	public ItemType (ItemStack stack) {
		item = stack.clone();
		name = getDefaultName();
	}
	
	public ItemStack toItemStack (int quantity) {
		ItemStack stack = item.clone();
		stack.setAmount(quantity);
		return stack;
	}
	
	private String getDefaultName () {
		return item.getType().toString().toLowerCase().replaceAll("_", " ");
	}
	
	public String getName () {
		return name;
	}
	
	public void setName (String newname) {
		name = newname;
	}
	
	public Material getMaterial () {
		return item.getType();
	}
	
	public int getMaxStackSize () {
		return item.getMaxStackSize();
	}
	
	public boolean isSimilar (ItemType other) {
		return item.isSimilar(other.item);
	}
	


}
