package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class DispenserFill extends JavaPlugin {
	private FileConfiguration config = getConfig();
	private FillSystem fillSystem;
	
	@Override
	public void onEnable() {
		config = getConfig();
		config.addDefault("maxRadius", 50);
		config.addDefault("complexityPerCycle", 5);
		config.addDefault("taskTickInterval", 5);
		config.addDefault("fullChunkHeight", false);
		config.options().copyDefaults(true);
		saveConfig();
		
		Messages.loadMessages(this);
		
		fillSystem = new FillSystem(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (cmd.getName().equalsIgnoreCase("tntfill")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Messages.get("runPlayerOnly"));
				return true;
			}
			
			ItemType item = new ItemType(Material.TNT);
			item.setName("TNT");
			
			return fillDispensersCommand ((Player)sender, item, args);
			
		} else if (cmd.getName().equalsIgnoreCase("dispenserfill")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Messages.get("runPlayerOnly"));
				return true;
			}
			
			Player p = (Player) sender;
			
			ItemStack stack = p.getItemInHand();
			ItemType item = new ItemType(stack);
			
			if (item.getMaterial().equals(Material.AIR)) {
				sender.sendMessage(Messages.get("noItemInHand"));
				return true;
			}
			
			return fillDispensersCommand (p, item, args);
			
		} else if (cmd.getName().equalsIgnoreCase("unfillall")) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(Messages.get("runPlayerOnly"));
				return true;
			}
			
			
			Player player = (Player) sender;
			int radius = config.getInt("maxRadius");
			
			fillSystem.clearDispensers(player, radius);
			
			return true;
		}
		
		return false;
	}
	
	private boolean fillDispensersCommand (Player player, ItemType item, String[] args) {
		int radius = config.getInt("maxRadius");
		FillMode fillMode = FillMode.AUTO;
		
		if (args.length > 2) {
			player.sendMessage(Messages.get("tooManyArguments"));
			return false;
		}
		
		if (args.length >= 1) {
			String mode = args[0].toUpperCase();
			
			if (mode.equalsIgnoreCase("?") || mode.equalsIgnoreCase("help")) {
				return false;
			}
			
			try {
				fillMode = FillMode.valueOf(mode);
			} catch (IllegalArgumentException e) {
				player.sendMessage(Messages.get("unrecognizedMode", mode));
				
				return false;
			}
		}
		if (args.length >= 2) {
			try {
				radius = Math.abs(Integer.parseInt(args[1]));
			} catch (NumberFormatException e) {
				player.sendMessage(Messages.get("radiusNotNumeric"));
				
				return false;
			}
		}
		
		
		if (!player.hasPermission("dispenserfill.overrideradius")) {
			radius = Math.min(radius, config.getInt("maxRadius"));
		}
		
		fillSystem.fillDispensers(player, item, fillMode, radius);
		return true;
	}
	
}
