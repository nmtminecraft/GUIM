package com.m0pt0pmatt.GUIM.IO;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

/**
 * 
 * @author James Pelster
 *
 */
public class MarketTabCompleter implements TabCompleter {
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length < 2) {
			if (sender instanceof Player) {
				if (((Player)sender).isOp()) {
					List<String> cmdList = new ArrayList<String>();
					cmdList.addAll(MarketCommand.getUserCommandList());
					for (String s : MarketCommand.getAdminCommandList()) {
						cmdList.add(s);
					}
					return cmdList;
				} else {
					return MarketCommand.getUserCommandList();
				}
			}
			
		}
		
		// /guim
		switch (args[0]) {
			case "create":
				
				break;
		}
		return null;
	}
}