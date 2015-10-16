package com.m0pt0pmatt.GUIM.IO;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.m0pt0pmatt.GUIM.GUIM;

/**
 * @author James Pelster
 */
public class MarketCommand implements CommandExecutor {

	/**
	 * A list of commands handled by the 'team survival' wrapping command
	 */
	private static final String[] adminCommandList = {"create", "reload"};

	private static final String[] userCommandList = {"help"};
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (args.length == 0) {
			//print usage
			sender.sendMessage("You must specify a sub-command. Maybe you should try /guim help");
			return false;
		}
		
		if (args[0].equalsIgnoreCase("help")) {
			onUserCommand(sender, args);
			return true;
		}
		
		if (args[0].equalsIgnoreCase("create")) {
			onAdminCommand(sender, args);
			return true;
		}
		
		return false;
	}
	
	protected static List<String> getAdminCommandList() {
		return Arrays.asList(adminCommandList);
	}

	protected static List<String> getUserCommandList() {
		return Arrays.asList(userCommandList);
	}
	
	/**
	 * Handles the admin commands
	 * @param sender
	 * @param args
	 * 
	 */
	private void onUserCommand(CommandSender sender, String[] args) {
		//[help]
		if(args.length < 1) {
			//print usage, cause we're no longer handled by onCommand!
			sender.sendMessage("GUIM Commands:");
			sender.sendMessage("/guim help - shows this");
			sender.sendMessage("/guim create [market name] - creates a new market with the specified name");
			sender.sendMessage("/guim reload - reloads the plugin");
			return;
		}
	}

	private void onMarketCreate(CommandSender sender, String[] args) {
		// /guim create [marketName]
		if(args.length != 2){
			sender.sendMessage("Incorrect number of arguments! \n");
			sender.sendMessage("usage: /guim create [marketName]");
			return;
		}
		
		System.out.println(GUIM.marketListener);
		System.out.println(sender);
		System.out.println(args[1]);
		GUIM.marketListener.setupMarket((Player) sender, args[1]);
		return;
	}

	/**
	 * Handles the admin 'session' command
	 * @param sender
	 * @param args
	 * @return
	 */
	private boolean onAdminCommand(CommandSender sender, String[] args) {
		//[list | create | start | stop | remove | info] ?
		//this is what EDFs has, and it feels pretty similar in terms of 'sessions'
		
		if (args[0].equalsIgnoreCase("create")) {
			onMarketCreate(sender, args);
			return true;
		}
		
		return false;
	}
}