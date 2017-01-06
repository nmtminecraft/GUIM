package com.m0pt0pmatt.GUIM.Player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;

import com.m0pt0pmatt.GUIM.MarketSale;
/**
 * This class keeps track of all variables for one player.
 * @author Matthew Broomfield, James Pelster
 */

public class PlayerInfo {
	
	/**
	 * which market the player is currently viewing.
	 * Set to null if the player is not at a market.
	 */
	public String currentMarket = null;
	
	/**
	 * Which page the player is currently viewing. Used when a menu has multiple pages/screens
	 */
	public int index = 0;
	
	/**
	 * The number of items a player currently has on the market
	 */
	public int slots = 0;
	
	/**
	 * The Inventory which the player is viewing.
	 */
	public Inventory inventory = null;
	
	/**
	 * This is to keep track when a player is in the process of buying or selling something on the market
	 */
	public MarketSale temp = null;
	
	public UUID name = null;
	
	/**
	 * how many units a player wants to buy/sell/etc
	 */
	public int unitQuantity = 1;
	
	/**
	 * whether or not the player is in the process of creating a market
	 */
	public boolean creatingMarket = false;
	
	/**
	 * temporary holder for an Access Block
	 */
	public Block accessBlock = null;
	
	/**
	 * temporary holder for a market name
	 */
	public String marketName = null;
	
	/**
	 * where the player is in a market
	 */
	public String menu = null;
	
	/**
	 * Creates a new PlayerInfo, given a player's name
	 * @param playerName
	 */
	public PlayerInfo(UUID playerName){
		name = playerName;
		currentMarket = null;
		index = 0;
		slots = 0;
		inventory = Bukkit.getServer().createInventory(Bukkit.getPlayer(playerName), 54, "Market: ");
	}
	
}
