package com.m0pt0pmatt.GUIM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Represents a Marketplace, for buying and selling items
 * 
 * @author Matthew
 * 
 */
public class Market{
	
	/**
	 * The List of items that are currently up for sale on the market
	 */
	public ArrayList<MarketSale> marketItems;
	public ArrayList<MarketSale> freeItems;
	public ArrayList<MarketSale> requestedItems;
	
	private ConfigManager configuration;
	
	private Set<Location> accessBlocks;
	private String name = null;
	private String owner = null;

	/**
	 * Default Constructor
	 */
	public Market(String owner, String name, Location accessBlock, JavaPlugin plugin) {
		//create internal objects
		marketItems = new ArrayList<MarketSale>();
		freeItems = new ArrayList<MarketSale>();
		requestedItems = new ArrayList<MarketSale>();
		
		//set the names
		this.name = name;
		this.owner = owner;
		
		//set access block
		this.accessBlocks = new HashSet<Location>();
		this.accessBlocks.add(accessBlock);
		
		this.configuration = new ConfigManager(plugin, (owner + "--" + name + ".yml"));
		
		//load the market from file
		load();
		
	}
	
	public Set<Location> getAccessBlocks(){
		return accessBlocks;
	}
	
	/**
	 * Save market data to file
	 */
	public void save(){
		configuration.reloadConfig();
		
		//configuration.getFile().delete();
		HashMap<String, Map<String, Object>> marketItems;
		HashMap<String, Map<String, Object>> requestedItems;
		HashMap<String, Map<String, Object>> freeItems;
		
		
		configuration.getConfig().set("owner", owner);
		configuration.getConfig().set("name", name);
		
		HashMap<String, Object> locations = new HashMap<String, Object>();
		int i = 0;
		for (Object location: accessBlocks.toArray()){
			HashMap<String, Object> lmap = new HashMap<String, Object>();
			lmap.put("world", ((Location)location).getWorld().getName());
			lmap.put("x", ((Location)location).getX());
			lmap.put("y", ((Location)location).getY());
			lmap.put("z", ((Location)location).getZ());
			lmap.put("pitch", ((Location)location).getPitch());
			lmap.put("yaw", ((Location)location).getYaw());
			locations.put(String.valueOf(i), lmap);
		}
		
		configuration.getConfig().createSection("accessLocations", locations);
		
		i = 0;
		marketItems = new HashMap<String, Map<String, Object>>();
		for (MarketSale m: this.marketItems){
			marketItems.put(Integer.toString(i), m.serialize());
			i++;
		}
		configuration.getConfig().createSection("marketItems", marketItems);
		
		i = 0;
		requestedItems = new HashMap<String, Map<String, Object>>();
		for (MarketSale m: this.requestedItems){
			requestedItems.put(Integer.toString(i), m.serialize());
			i++;
		}
		configuration.getConfig().createSection("requestedItems", requestedItems);
		
		i = 0;
		freeItems = new HashMap<String, Map<String, Object>>();
		for (MarketSale m: this.freeItems){
			freeItems.put(Integer.toString(i), m.serialize());
			i++;
		}
		configuration.getConfig().createSection("freeItems", freeItems);	
		configuration.saveConfig();
		
		System.out.println("[HomeWorldPlugin] Saved market");
		
	}

	/**
	 * load market data from file
	 */
	public void load(){
//
//		//clear the old market
//		market.clear();
//		
//		//get each item
//		MemorySection itemlocation = (MemorySection) configuration.getConfig().get("items");
//		
//		if (itemlocation == null){
//			return;
//		}
//		
//		Set<String> items = itemlocation.getKeys(false);
//		for (String index: items){
//
//			//get the properties
//			Set<String> itemProperties = ((MemorySection) itemlocation.get(index)).getKeys(false);
//			
//			//create a new mapping for the properties
//			Map<String, Object> map = new HashMap<String, Object>();
//			
//			//add the properties to the new map
//			for (String s: itemProperties){
//				
//				//fix for enchantments
//				if (s.equals("enchantments")){
//					//get all the enchantments
//					Set<String> enchantments = ((MemorySection)itemlocation.get(index + "." + s)).getKeys(false);
//					
//					Map<String, Object> enchantmentMap = new HashMap<String, Object>();
//					for (String enchantment: enchantments){
//						enchantmentMap.put(enchantment, itemlocation.get(index + "." + s + "." + enchantment));
//					}
//
//					map.put(s, enchantmentMap);
//				}
//				else{
//					map.put(s, itemlocation.get(index + "." + s));
//				}
//				
//			}
//			
//			//create and add the new item
//			MarketSale item = MarketSale.deserialize(map);
//			market.add(item);
//		}
//		
//		System.out.println("[HomeWorldPlugin] Loaded Market");
	}

	public String getFullName() {
		// TODO Auto-generated method stub
		return owner + "--" + name;
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
	
	
}
