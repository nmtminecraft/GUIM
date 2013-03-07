package com.m0pt0pmatt.GUIM;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Main class for the plugin. Everything starts from here
 * @author Matthew
 *
 */
public class GUIM extends JavaPlugin{

	/**
	 * hook for the economy
	 */
	public static Economy economy = null;
	
	/**
	 * A mapping of all markets
	 */
	public static HashMap<String, Market> marketNames = null;
	public static HashMap<Location, Market> marketLocations = null;
	
	/**
	 * This maps players name to the information this plugin requires of them.
	 */
	private static HashMap<String, PlayerInfo> playerInfo;
	
	/**
	 * The configuration manager for the plugin.
	 */
	public static ConfigManager mainConfig = null;
	
	/**
	 * The thread which auto-saves files
	 */
	public static FileSavingThread savingThread = null;
	
	public static MarketListener marketListener = null;
	
	
	/**
	 * This is ran once the plugin is enabled. It is ran after the constructor.
	 */
	public void onEnable(){		
		
		//set up configurations
		mainConfig = new ConfigManager(this, "config.yml");
		
		//create the market map
		marketNames = new HashMap<String, Market>();
		marketLocations = new HashMap<Location, Market>();
			
		//create the playerInfo map
		playerInfo = new HashMap<String, PlayerInfo>();
		
		//setup economy hook
		setupEconomy();
		
		//load the markets
		//load();
		
		//set up the thread that saves data
		savingThread = new FileSavingThread(this);
		savingThread.start();
		
		//registers the market listener
		marketListener = new MarketListener(this);
		Bukkit.getServer().getPluginManager().registerEvents(marketListener, this);
		
		getLogger().info("GUIMarket has been enabled.");
	}
 
	/**
	 * ran when the plugin is being disabled. saves the houses to file.
	 */
	public void onDisable(){
		//save content
		save();
		
		getLogger().info("GUIMarket has been disabled.");
	}
	
	/**
	 * Reload method. Makes sure all data is saved to file.
	 */
	public void onReload(){
		//save content
		save();
		
		//load content
		load();
		
		getLogger().info("GUIMarket has been reloaded.");
	}
	

	/**
	 * Command handler.
	 */
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if(cmd.getName().equalsIgnoreCase("GUIM")){
			if (args.length == 0){
				//display help
				return true;
			}
			else if (args.length == 1){
				if (args[0].equals("help")){
					//display help
					return true;
				}
				else{
					return false;
				}
			}
			else if (args.length == 2){
				if (args[0].equals("create")){
					marketListener.setupMarket((Player) sender, args[1]);
				}
				
				
				return true;
			}
			else if (args.length == 3){
				return true;
			}
			else{
				return false;
			}
		}
		
		return false;
	}
	
	/**
	 * Uses Vault to hook into an economy plugin
	 * @return
	 */
	private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
	
	/**
	 * Saves everything
	 */
	public void save() {
		
		//save the markets
		for (Market market: marketNames.values()){
			market.save();
		}
		
	}
	
	/**
	 * loads everything
	 */
	public void load(){
		
		//clear old markets
		marketNames.clear();
		marketLocations.clear();
		
		//get all the names of the markets
		for (File file: this.getDataFolder().listFiles()){
			if (!file.getName().equals("config.yml")){
				//create new market
				addMarket(file);
			}
		}
		
	}


	
	
	
	private void addMarket(File file) {
		ConfigManager cm = new ConfigManager(this, file.getName());
		FileConfiguration config = cm.getConfig();
		
		//get the name and owner of the market
		String name = (String)config.get("name");
		String fullName = (String)config.get("name");
		String owner = fullName.split("--")[0];
		
		//get the access locations
		HashSet<Location> locations = this.getLocations(config);
		
		//get the items that are on the market
		ArrayList<MarketSale> marketItems = this.getSales(config, "marketItems");
		
		//get the items that have been requested
		ArrayList<MarketSale> requestedItems = this.getSales(config, "requestedItems");
		
		//get the free items
		ArrayList<MarketSale> freeItems = this.getSales(config, "freeItems");
		
		//create the market
		Market market = new Market(owner, name, locations, this);
		market.marketItems = marketItems;
		market.requestedItems = requestedItems;
		market.freeItems = freeItems;
		
		//add the market
		marketNames.put(fullName, market);
		for (Location location: locations){
			marketLocations.put(location, market);
		}
		
		
	}
	
	private HashSet<Location> getLocations(FileConfiguration config){
		HashSet<Location> locations = new HashSet<Location>();
		
		//get the memory section
		MemorySection memory = (MemorySection) config.get("accessLocations");

		//for each location
		for (String index: memory.getKeys(false)){
			
			String world = null;
			double x = 0, y = 0, z = 0;
			float pitch = 0, yaw = 0;
			
			//for each property
			for (String s: ((MemorySection) memory.get(index)).getKeys(false)){
				if (s.equals("world")){
					world = (String) memory.get(index + "." + s);
				}
				else if (s.equals("x")){
					x = (Double) memory.get(index + "." + s);
				}
				else if (s.equals("y")){
					y = (Double) memory.get(index + "." + s);
				}
				else if (s.equals("z")){
					z = (Double) memory.get(index + "." + s);
				}
				else if (s.equals("pitch")){
					pitch = (Float) memory.get(index + "." + s);
				}
				else if (s.equals("yaw")){
					yaw = (Float) memory.get(index + "." + s);
				}
			}
			
			locations.add(new Location(Bukkit.getWorld(world), x, y, z, pitch, yaw));
			
		}
		return locations;
	}
	
	private ArrayList<MarketSale> getSales(FileConfiguration config, String whichList){
		MemorySection memory = (MemorySection) config.get(whichList);
		ArrayList<MarketSale> sales = new ArrayList<MarketSale>();
		
		
		for (String index: memory.getKeys(false)){
			LinkedList<ItemStack> items = new LinkedList<ItemStack>();
			
			//get the properties
			Set<String> saleProperties = ((MemorySection) memory.get(index)).getKeys(false);
			Map<String, Object> saleMap = new HashMap<String, Object>();
			
			//add the properties to the new map
			for (String s: saleProperties){
			    if (s.equals("items")){
			    	Set<String> itemProperties = ((MemorySection) memory.get(index + ".items")).getKeys(false);
			    	Map<String, Object> itemMap = new HashMap<String, Object>();
			    	for (String si: itemProperties){
		    				
	    				//fix for enchantments
	    				if (si.equals("enchantments")){
	    					//get all the enchantments
	    					Set<String> enchantments = ((MemorySection)memory.get(index + "." + s + "." + si)).getKeys(false);
	    					
	    					Map<String, Object> enchantmentMap = new HashMap<String, Object>();
	    					for (String enchantment: enchantments){
	    						enchantmentMap.put(enchantment, memory.get(index + "." + s + "." + si + "." + enchantment));
	    					}

	    					itemMap.put(si, enchantmentMap);
	    				}
	    				else{
	    					itemMap.put(si, memory.get(index + "." + s + "." + si));
	    				}
	    				
			    	}
		    			
	    			//create and add the new item
	    			ItemStack item = ItemStack.deserialize(itemMap);
	    			items.add(item);
			    	
			    	
			    }else {
			    	saleMap.put(s, memory.get(index + "." + s));
			    }
			    saleMap.put("items", items);
			}
			
			//create and add the new sale
			sales.add(MarketSale.deserialize(saleMap));
		}
		
		return sales;
	}
	

	public static PlayerInfo getPlayerInfo(String playerName){
		return playerInfo.get(playerName);
	}
	
	public static void addPlayerInfo(String playerName){
		playerInfo.put(playerName, new PlayerInfo(playerName));
	}

	
	
	
	public static void updateMenu(String marketName, String whichMenu){
		String menu;
		for (PlayerInfo pInfo: playerInfo.values()){
			menu = pInfo.menu;
			if (menu.equals(whichMenu)){
				//players menu needs to be updated
				MenuPainter.paintMenu(Bukkit.getPlayer(pInfo.name));
			}
		}
		
	}
	
}

