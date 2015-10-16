package com.m0pt0pmatt.GUIM;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.m0pt0pmatt.GUIM.IO.MarketCommand;
import com.m0pt0pmatt.GUIM.IO.MarketTabCompleter;
import com.m0pt0pmatt.bettereconomy.BetterEconomy;
import com.m0pt0pmatt.bettereconomy.EconomyManager;
import com.m0pt0pmatt.bettereconomy.accounts.UUIDFetcher;


/**
 * Main class for the plugin. Everything starts from here
 * @author Matthew
 *
 */
public class GUIM extends JavaPlugin {

	/**
	 * hook for the economy
	 */
	public static EconomyManager economy = null;
	
	/**
	 * A mapping of all markets
	 */
	public static HashMap<String, Market> marketNames = null;
	public static HashMap<Location, Market> marketLocations = null;
	
	/**
	 * This maps players name to the information this plugin requires of them.
	 */
	private static HashMap<UUID, PlayerInfo> playerInfo;
	
	public static MarketListener marketListener = null;
	
	
	/**
	 * This is ran once the plugin is enabled. It is ran after the constructor.
	 */
	public void onEnable() {	
		this.getCommand("guim").setExecutor(new MarketCommand());
		this.getCommand("guim").setTabCompleter(new MarketTabCompleter());
		
		//create the market map
		marketNames = new HashMap<String, Market>();
		marketLocations = new HashMap<Location, Market>();
			
		//create the playerInfo map
		playerInfo = new HashMap<UUID, PlayerInfo>();
		
		//setup economy hook
		if (setupEconomy()) {
			getLogger().info("Hooked into Vault Economy.");
		} else {
			getLogger().warning("Vault Economy could not be found. Will try again later on a need basis.");
		}
				
		//load the markets
		load();
		
		//registers the market listener
		marketListener = new MarketListener(this);
		Bukkit.getServer().getPluginManager().registerEvents(marketListener, this);
		getLogger().info("Listener registered.");
		
		getLogger().info("GUIMarket has been enabled.");
	}
 
	/**
	 * ran when the plugin is being disabled. saves the houses to file.
	 */
	public void onDisable() {
		//save content
		save();
		
		getLogger().info("GUIMarket has been disabled.");
	}
	
	/**
	 * Reload method. Makes sure all data is saved to file.
	 */
	public void onReload() {
		//save content
		save();
		
		//load content
		load();
		
		getLogger().info("GUIMarket has been reloaded.");
	}
	

	/**
	 * Command handler.
	 */
	
	
	/**
	 * Uses Vault to hook into an economy plugin
	 * @return
	 */
	public static boolean setupEconomy() {
        if (Bukkit.getPluginManager().isPluginEnabled("BetterEconomy")) {
            economy = BetterEconomy.economy;
            return true;
        }

        return (false);
    }
	
	/**
	 * Saves everything
	 */
	public void save() {
		getLogger().info("Saving markets to file");
		
		//save the markets
		for (Market market : marketNames.values()) {
			market.save();
		}
		
		getLogger().info("Markets have been saved successfully");
		
	}
	
	/**
	 * loads everything
	 */
	public void load() {
		getLogger().info("Loaded markets from file");
		
		//clear old markets
		marketNames.clear();
		marketLocations.clear();
		getLogger().info("Old markets are cleaned");
		
		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}
		
		//get all the names of the markets
		for (File file : this.getDataFolder().listFiles()){
			if (!file.getName().equals("config.yml")) {
				//create new market
				addMarket(file);
			}
		}
		
		getLogger().info("Markets have been loaded successfully");
		
	}	
	
	private void addMarket(File file) {
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		
		//get the name and owner of the market
		String name = (String)config.get("name");
		UUID owner = null;
		try {
			owner = UUID.fromString(config.getString("owner"));
		} catch (IllegalArgumentException e) {
			try {
				owner = UUIDFetcher.getUUIDOf(config.getString("owner"));
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		
		String fullName = "";
		if (owner != null) {
			fullName += Bukkit.getOfflinePlayer(owner).getName()+" -- ";
		}
		fullName += name;
		
		//get the access locations
		HashSet<Location> locations = this.getLocations(config);
		
		//get the items that are on the market
		ArrayList<MarketSale> marketItems = this.getSales(config, "marketItems");
		
		//get the items that have been requested
		ArrayList<MarketSale> requestedItems = this.getSales(config, "requestedItems");
		
		//get the free items
		ArrayList<MarketSale> freeItems = this.getSales(config, "freeItems");
		
		HashMap<UUID, Integer> numSales = new HashMap<UUID, Integer>();
		MemorySection memory = (MemorySection) config.get("currentSales");
		
		//for each player
		for (String playerName: memory.getKeys(false)) {
			try {
				numSales.put(UUID.fromString(playerName), (Integer) memory.get(playerName));
			} catch (IllegalArgumentException e) {
				OfflinePlayer p = null;
				try {
					p = Bukkit.getOfflinePlayer(UUIDFetcher.getUUIDOf(playerName));
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				
				if (p != null) {
					numSales.put(p.getUniqueId(), (Integer) memory.get(playerName));
				} else {
					getLogger().warning("Unable to determine player:\n" + ChatColor.RED + playerName + ChatColor.RESET);
				}
			}
		}
		
		//create the market
		Market market = new Market(owner, name, locations, numSales, this);
		market.marketItems = marketItems;
		market.requestedItems = requestedItems;
		market.freeItems = freeItems;
		
		//add the market
		marketNames.put(fullName, market);
		for (Location location : locations) {
			marketLocations.put(location, market);
		}
		
		
	}
	
	private HashSet<Location> getLocations(FileConfiguration config) {
		HashSet<Location> locations = new HashSet<Location>();
		
		//get the memory section
		MemorySection memory = (MemorySection) config.get("accessLocations");

		//for each location
		for (String index: memory.getKeys(false)) {
			String world = null;
			double x = 0, y = 0, z = 0;
			
			//for each property
			for (String s : ((MemorySection)memory.get(index)).getKeys(false)) {
				if (s.equals("world")) {
					world = (String) memory.get(index + "." + s);
				} else if (s.equals("x")) {
					x = (Double) memory.get(index + "." + s);
				} else if (s.equals("y")) {
					y = (Double) memory.get(index + "." + s);
				} else if (s.equals("z")) {
					z = (Double) memory.get(index + "." + s);
				}
			}
			
			locations.add(new Location(Bukkit.getWorld(world), x, y, z));
			
		}
		return locations;
	}
	
	private ArrayList<MarketSale> getSales(FileConfiguration config, String whichList) {
		MemorySection memory = (MemorySection) config.get(whichList);
		ArrayList<MarketSale> sales = new ArrayList<MarketSale>();		
		
		for (String index : memory.getKeys(false)) {
			
			LinkedList<ItemStack> items = new LinkedList<ItemStack>();
			
			//get the properties
			Set<String> saleProperties = ((MemorySection) memory.get(index)).getKeys(false);
			Map<String, Object> saleMap = new HashMap<String, Object>();
			
			//add the properties to the new map
			for (String s: saleProperties) {
			    if (s.equals("items")) {
			    	Set<String> itemKeys = ((MemorySection) memory.get(index + ".items")).getKeys(false);
			    	for (String itemKey : itemKeys) {
			    					    		
			    		//get the properties
						Set<String> itemProperties = ((MemorySection) memory.get(index + ".items." + itemKey)).getKeys(false);
						
						//create a new mapping for the properties
						Map<String, Object> map = new HashMap<String, Object>();
						
						//add the properties to the new map
						for (String property : itemProperties) {
														
							//fix for enchantments
							if (property.equals("enchantments")) {
								//get all the enchantments
								Set<String> enchantments = ((MemorySection)memory.get(index + ".items." + itemKey + "." + property)).getKeys(false);
								
								Map<String, Object> enchantmentMap = new HashMap<String, Object>();
								for (String enchantment : enchantments) {
									enchantmentMap.put(enchantment, memory.get(index + ".items." + itemKey + "." + property + "." + enchantment));
								}

								map.put(property, enchantmentMap);
							} else {
								map.put(property, memory.get(index + ".items." + itemKey + "." + property));
							}
							
						}
						
						//create and add the new item
		    			ItemStack item = ItemStack.deserialize(map);
		    			items.add(item);		
	    				
			    	}
			    	
	    			saleMap.put("items", items);
			    	
			    } else {
			    	saleMap.put(s, memory.get(index + "." + s));
			    }
			    
			}
			
			//create and add the new sale
			sales.add(MarketSale.deserialize(saleMap));
		}
		
		return sales;
	}
	

	public static PlayerInfo getPlayerInfo(UUID playerName) {
		return playerInfo.get(playerName);
	}
	
	public static void addPlayerInfo(UUID playerName) {
		playerInfo.put(playerName, new PlayerInfo(playerName));
	}

	
	
	
	public static void updateMenu(String marketName, String whichMenu) {
		String menu;
		for (PlayerInfo pInfo : playerInfo.values()) {
			menu = pInfo.menu;
			if (menu != null) {
				if (menu.equals(whichMenu)) {
					//players menu needs to be updated
					MenuPainter.paintMenu(Bukkit.getPlayer(pInfo.name));
				}
			}
		}
	}
	
}

