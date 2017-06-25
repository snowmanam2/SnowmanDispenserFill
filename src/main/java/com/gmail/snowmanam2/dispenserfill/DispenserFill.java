package com.gmail.snowmanam2.dispenserfill;

import org.bukkit.GameMode;
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
		config.addDefault("defaultMode", FillMode.AUTO.toString());
		config.options().copyDefaults(true);
		saveConfig();
		
		Messages.loadMessages(this);
		
		fillSystem = new FillSystem(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(Messages.get("runPlayerOnly"));
			return true;
		}
		
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("tntfill")) {
			
			ItemType item = new ItemType(Material.TNT);
			item.setName("TNT");
			
			if (!fillDispensersCommand (p, item, args))
			{
				p.sendMessage(Messages.get("usageFill", cmd.getName(), config.get("maxRadius")));
			}
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("dispenserfill")) {
			ItemStack stack = p.getItemInHand();
			ItemType item = new ItemType(stack);
			
			if (item.getMaterial().equals(Material.AIR)) {
				sender.sendMessage(Messages.get("noItemInHand"));
				return true;
			}
			
			if (!fillDispensersCommand (p, item, args))
			{
				p.sendMessage(Messages.get("usageFill", cmd.getName(), config.get("maxRadius")));
			}
			
			return true;
			
		} else if (cmd.getName().equalsIgnoreCase("unfillall")) {			
			
			int radius = config.getInt("maxRadius");
			fillSystem.clearDispensers(p, radius);
			
			return true;
		}
		
		return true;
	}
	
	private FillMode getDefaultMode (Player player) {
		FillMode fillMode;
		
		try {
			fillMode = FillMode.valueOf(config.getString("defaultMode").toUpperCase());
		} catch (IllegalArgumentException e) {
			fillMode = FillMode.AUTO;
		}
		
		if (player.getGameMode().equals(GameMode.CREATIVE) && player.hasPermission("dispenserfill.fillall")) {
			fillMode = FillMode.FILLALL;
		}
		
		return fillMode;
	}
	
	private boolean fillDispensersCommand (Player player, ItemType item, String[] args) {
		int radius = config.getInt("maxRadius");
		FillMode fillMode = getDefaultMode(player);
		
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
		
		if (!player.hasPermission("dispenserfill.fillall") && fillMode == FillMode.FILLALL) {
			player.sendMessage(Messages.get("fill.noPermission"));
			return true;
		}
		
		fillSystem.fillDispensers(player, item, fillMode, radius);
		return true;
	}
	
}
