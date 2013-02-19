package com.m0pt0pmatt.GUIM;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import edu.nmt.minecraft.HomeWorldPlugin.market.MarketItemStack;


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
		Location location;
		String world = null;
		double x, y, z;
		float pitch, yaw;
		HashSet<Location> locations;
		
		//get the memory section
		MemorySection accessLocationMemory = (MemorySection) config.get("accessLocations");
		//get each location
		Set<String> accessLocations = accessLocationMemory.getKeys(false);
		//for each location
		for (String index: accessLocations){

			//get the properties
			Set<String> properties = ((MemorySection) accessLocationMemory.get(index)).getKeys(false);		
			
			//for each property
			for (String s: properties){
				if (s.equals("world")){
					world = (String) accessLocationMemory.get(index + "." + s);
				}
				else if (s.equals("x")){
					x = (Double) accessLocationMemory.get(index + "." + s);
				}
				else if (s.equals("y")){
					y = (Double) accessLocationMemory.get(index + "." + s);
				}
				else if (s.equals("z")){
					z = (Double) accessLocationMemory.get(index + "." + s);
				}
				else if (s.equals("pitch")){
					pitch = (String) accessLocationMemory.get(index + "." + s);
				}
				else if (s.equals("yaw")){
					yaw = (String) accessLocationMemory.get(index + "." + s);
				}
			}
			
			Location location = new Location(null, 0, 0, 0, 0, 0);
			
		}
		
		MemorySection marketItems = (MemorySection) config.get("marketItems");
		
		//Market market;
		
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

