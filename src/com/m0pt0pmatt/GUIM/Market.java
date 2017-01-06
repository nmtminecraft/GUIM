package com.m0pt0pmatt.GUIM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a Marketplace, for buying and selling items
 * 
 * @author Matthew Broomfield, James Pelster
 */
public class Market {
	
	/**
	 * The List of items that are currently up for sale on the market
	 */
	public ArrayList<MarketSale> marketItems;
	public ArrayList<MarketSale> freeItems;
	public ArrayList<MarketSale> requestedItems;
    public static ArrayList<MarketSale> globalMarketItems;
	
	private String configFilename;
	
	private Set<Location> accessBlocks;
	private String name = null;
	private UUID owner = null;
	public Map<UUID, Integer> numSales;
	public static int maxNumber = 10;
	private JavaPlugin plugin;

	/**
	 * Default Constructor
	 */
	public Market(UUID owner, String name, Set<Location> accessBlocks, Map<UUID, Integer> numSales, JavaPlugin plugin) {
		//create internal objects
		marketItems = new ArrayList<MarketSale>();
		freeItems = new ArrayList<MarketSale>();
		requestedItems = new ArrayList<MarketSale>();
		if (globalMarketItems == null) globalMarketItems = new ArrayList<MarketSale>();

		this.numSales = numSales;
		
		//set the names
		this.name = name;
		this.owner = owner;
		
		//set access block
		this.accessBlocks = new HashSet<Location>();
		this.accessBlocks = accessBlocks;
		this.configFilename = owner + "--" + name + ".yml";
		this.plugin = plugin;
		
	}
	
	public Set<Location> getAccessBlocks() {
		return accessBlocks;
	}
	
	/**
	 * Save market data to file
	 */
	public void save() {
		YamlConfiguration configuration = new YamlConfiguration();
		
		//configuration.getFile().delete();
		HashMap<String, Map<String, Object>> marketItems;
		HashMap<String, Map<String, Object>> requestedItems;
		HashMap<String, Map<String, Object>> freeItems;		
		
		configuration.set("owner", owner.toString());
		configuration.set("name", name);
		
		HashMap<String, Object> locations = new HashMap<String, Object>();
		int i = 0;
		for (Object location : accessBlocks.toArray()) {
			HashMap<String, Object> lmap = new HashMap<String, Object>();
			lmap.put("world", ((Location)location).getWorld().getName());
			lmap.put("x", ((Location)location).getX());
			lmap.put("y", ((Location)location).getY());
			lmap.put("z", ((Location)location).getZ());
			lmap.put("pitch", ((Location)location).getPitch());
			lmap.put("yaw", ((Location)location).getYaw());
			locations.put(String.valueOf(i), lmap);
		}
		
		configuration.createSection("accessLocations", locations);
		
		i = 0;
		marketItems = new HashMap<String, Map<String, Object>>();
		for (MarketSale m : this.marketItems){
			marketItems.put(Integer.toString(i), m.serialize());
			i++;
		}
		configuration.createSection("marketItems", marketItems);
		
		i = 0;
		requestedItems = new HashMap<String, Map<String, Object>>();
		for (MarketSale m : this.requestedItems) {
			requestedItems.put(Integer.toString(i), m.serialize());
			i++;
		}
		configuration.createSection("requestedItems", requestedItems);
		
		i = 0;
		freeItems = new HashMap<String, Map<String, Object>>();
		for (MarketSale m : this.freeItems) {
			freeItems.put(Integer.toString(i), m.serialize());
			i++;
		}
		configuration.createSection("freeItems", freeItems);
		
		configuration.createSection("currentSales", numSales);
		try {
			File configFile = new File(plugin.getDataFolder(), configFilename);
			configuration.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("[GUIM] Saved market to " + configFilename);
		
	}

	public String getFullName() {
		String fullName = "";
		if (owner != null) {
			fullName += Bukkit.getOfflinePlayer(owner).getName()+" -- ";
		}
		fullName += name;
		return fullName;
	}
	
	public String getReadableName() {
		return Bukkit.getPlayer(owner).getName() + "--" + name;
	}


	public MarketSale getItem(int i, String whichList) {
		if (whichList.equals("market")){
			return marketItems.get(i);
		}
		else if (whichList.equals("request")){
			return requestedItems.get(i);
		}
		else if (whichList.equals("free")){
			return freeItems.get(i);
		}
		return null;
		
	}
	
	public int getNumSales(UUID playerName){
		Integer num = numSales.get(playerName);
		if (num == null){
			return -1;
		}
		else return num;
	}

	public void addPlayer(UUID playerName) {
		numSales.put(playerName, 0);
	}

	public void decrementPlayer(UUID playerName) {
		int num = numSales.get(playerName);
		num = num - 1;
		if (num < 0){
			num = 0;
		}
		numSales.remove(playerName);
		numSales.put(playerName, num);
		
	}
	
	public void incrementPlayer(UUID playerName) {
		int num = numSales.get(playerName);
		num = num++;
		numSales.remove(playerName);
		numSales.put(playerName, num);
		
	}
	
}
