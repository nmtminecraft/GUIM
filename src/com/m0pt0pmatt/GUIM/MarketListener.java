package com.m0pt0pmatt.GUIM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.m0pt0pmatt.GUIM.IO.HelpBookCreator;
import com.m0pt0pmatt.GUIM.Player.PlayerInfo;
import com.m0pt0pmatt.bettereconomy.io.EconomyLoadEvent;

/**
 * The listener that handles all event interaction.</br>
 * <b>Warning:</b> this class is a crazy beast, not a sexy one.
 * @author Matthew Broomfield, James Pelster
 */
public class MarketListener implements Listener {
	
	JavaPlugin plugin;
	
	public MarketListener(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onEconomyLoad(EconomyLoadEvent event) {
		if (GUIM.economy == null && event.getEconomy() != null) {
			GUIM.economy = event.getEconomy();
			plugin.getLogger().info("Successfully hooked into BetterEconomy!");
		}
	}
	
	
	/**
	 * Event that is triggered when the player clicks.
	 * Checks if the player clicked on a access block.
	 * If so, trigger the appropriate market
	 * @param event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void marketStart(PlayerInteractEvent event) {
		//get the player and the PlayerInfo
		Player player = event.getPlayer();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		
		//check if the player clicked a block
		if (event.getClickedBlock() == null){
			return;
		}

		// create first time data if needed
		if (playerInfo == null) {
			GUIM.addPlayerInfo(player.getUniqueId());
			playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		}
		
		//check if the player clicked on an access block
		Market market = GUIM.getMarketLocations().get(event.getClickedBlock().getLocation());
		if (market == null) {
			return;
		}
		
		//add a player count if there doesn't exist one
		if (market.getNumSales(player.getUniqueId()) == -1) {
			market.addPlayer(player.getUniqueId());
		}
		
		//if there is no economy, dont try anything
		if (GUIM.economy == null) {
			if (!GUIM.setupEconomy()) {
				event.getPlayer().sendMessage("Error. No economy plugin found. Cannot use markets.");
				return;
			}
		}
		
		//set the playerInfo correctly
		playerInfo.currentMarket = market.getFullName();
		playerInfo.menu = "main";
		
		// paint the main menu
		MenuPainter.paintMenu(player);

		//open the inventory for the player
		player.openInventory(playerInfo.inventory);
	}
	
	
	/**
	 * Event triggered when an item is clicked in the inventory
	 * @param event
	 */
	@EventHandler(ignoreCancelled=true)
	public void itemClicked(InventoryClickEvent event) {
		//get the player and the PlayerInfo
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		
		//If there is no PlayerInfo, then the player can't possibly be at a market
		if (playerInfo == null) {
			return;
		}
		
		//don't do anything if the player isn't in the marketplace
		if (playerInfo.currentMarket == null) {
			return;
		}
		Market market = GUIM.markets.get(playerInfo.currentMarket);

		//don't allow any right clicking
		if (event.isRightClick()) {
			event.setResult(org.bukkit.event.Event.Result.DENY);
	        event.setCancelled(true);
	        return;
		}
		
		//make sure the slot is in bounds (not off the screen)
		if (event.getSlot() < 0) {
			return;
		}

		//choose the right menu
		String[] parts = playerInfo.menu.split(":");
		switch (parts[0]) {
            case "main":
                mainMenuEvent(event, market);
                break;
            case "market":
                switch (parts[1]) {
                    case "view":
                        viewItemsMenuEvent(event, market);
                        break;
                    case "buy":
                        buyMenuEvent(event, market);
                        break;
                    case "sell":
                        sellMenuEvent(event, market);
                        break;
                }
                break;
            case "request":
                switch (parts[1]) {
                    case "view":
                        viewItemsMenuEvent(event, market);
                        break;
                    case "buy":
                        buyMenuEvent(event, market);
                        break;
                    case "confirm":
                        confirmMenuEvent(event, market);
                        break;
                }
                break;
            case "free":
                switch (parts[1]) {
                    case "view":
                        viewItemsMenuEvent(event, market);
                        break;
                    case "buy":
                        buyMenuEvent(event, market);
                        break;
                    case "sell":
                        sellMenuEvent(event, market);
                        break;
                }
                break;
            case "options":
                optionsMenuEvent(event, market);
                break;
            case "admin":
                switch (parts[1]) {
                    case "view":
                        adminMenuEvent(event, market);
                        break;
                    case "sellserver":
                        sellMenuEvent(event, market);
                        break;
                    case "sellglobal":
                        sellMenuEvent(event, market);
                        break;
                }
                break;
		}
		
		//cancel the event (Stop the player from picking up items)
		event.setResult(org.bukkit.event.Event.Result.DENY);
        event.setCancelled(true);
	}


	/**
	 * Event for when a player closes out a market's inventory
	 * @param event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void marketClosed(InventoryCloseEvent event) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(event.getPlayer().getUniqueId());
		if (playerInfo != null){
			playerInfo.currentMarket = null;
			playerInfo.temp = null;
		}
	}
	
	/**
	 * The market's main menu
	 * Acts as a gateway for the rest of the market
	 * @param event
	 * @param market
	 */
	private void mainMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();

		// click was made in the player's inventory
		if (event.getRawSlot() > event.getSlot()) {
			return;
		}
		
		//if view market items was pressed
		if (event.getSlot() == MenuPainter.getLeft(inv, 0)) {
			//set the menu
			playerInfo.menu = "market:view";
			playerInfo.index = 0;
			
			//paint the inventory
			MenuPainter.paintMenu(player);
		}
		
		//if requests menu was pressed
		else if (event.getSlot() == MenuPainter.getLeft(inv, 1)) {
			//set the menu
			playerInfo.menu = "request:view";
			playerInfo.index = 0;
			
			//paint the inventory
			MenuPainter.paintMenu(player);
		}
		
		//if free items menu was pressed
		else if (event.getSlot() == MenuPainter.getLeft(inv, 2)) {
			//set the menu
			playerInfo.menu = "free:view";
			playerInfo.index = 0;
			
			//paint the inventory
			MenuPainter.paintMenu(player);
		}
		
		//if options button was pressed
		else if (event.getSlot() == MenuPainter.getLeft(inv, 3)) {
			//set the menu
			playerInfo.menu = "options";
			
			//paint the inventory
			MenuPainter.paintMenu(player);
		}	
		
		//if admin button was pressed
		else if (event.getSlot() == MenuPainter.getLeft(inv, 4)) {
			//set the menu
			playerInfo.menu = "admin:view";
			
			//paint the inventory
			MenuPainter.paintMenu(player);
		}
		
		//if help button was pressed
		else if (event.getSlot() == MenuPainter.getLeft(inv, 5)) {
			HelpBookCreator.mainMenuHelp(player);
			return;
		}
	}
		
	/**
	 * What to do if the click was made while in a view items menu, for market, request, and free
	 * @param event
	 *            the InventoryClickEvent
	 */
	private static void viewItemsMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();
		String[] menu = playerInfo.menu.split(":");
		
		// click was made in the player's inventory
		if (event.getRawSlot() > event.getSlot()) {
			
			//if request menu and book
			if (menu[0].equals("request") && event.getCurrentItem().getType().equals(Material.WRITTEN_BOOK)){
				//make sure the player is allowed to 
				if (market.getNumSales(player.getUniqueId()) >= Market.maxNumber){
					player.sendMessage("You have too many things on this market.");
					return;
				}
				
				
				//if the book has correct syntax
				if (checkBook(player, event)){
					//goto the confirm trade request
					playerInfo.menu = menu[0].concat(":confirm");
					player.sendMessage("Are you sure you want to request these items?");
					MenuPainter.paintMenu(player);

					return;
				}
			}
			
			//else do nothing
			return;
		}

		// if previous button was pressed
		if (event.getSlot() == MenuPainter.getLeft(inv, 0)) {
			int index = playerInfo.index;

			// make sure we are not out of bounds
			if (index == 0) {
				return;
			}
			
			// re-adjust the index for the player
			playerInfo.index = (index - 1);
			
			// redraw the inventory
			MenuPainter.paintMenu(player);
			
			return;
		}
		
		// if next button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
			int index = playerInfo.index;

			// re-adjust the index for the player
			playerInfo.index = (index + 1);
			
			// redraw the inventory
			MenuPainter.paintMenu(player);

			return;
		}

		// if sell item button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 1) && !menu[0].equals("request")) {
			if (market.getNumSales(player.getUniqueId()) >= Market.maxNumber){
				player.sendMessage("You have too many things on this market.");
				return;
			}
			
			// set chest up for item menu
			player.sendMessage("Choose the items you wish to sell.");
			player.sendMessage("Then, choose how many of each item.");
			
			// set the player's menu
			playerInfo.menu = menu[0].concat(":sell:quantity");
			
			// paint the sell menu
			MenuPainter.paintMenu(player);
			return;
		}
		
		// if back button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 2)) {
			
			// set the player's menu
			playerInfo.menu = "main";
			
			// paint the sell menu
			MenuPainter.paintMenu(player);
			return;
		}
		
		//if help button was pressed
		else if (event.getSlot() == MenuPainter.getLeft(inv, 3)){
			//which menu
			if (playerInfo.menu.equals("market:view")){
				HelpBookCreator.viewMarketHelp(player);
			}
			else if (playerInfo.menu.equals("request:view")){
				HelpBookCreator.viewRequestHelp(player);
			}
			
			return;
			
		}

		// if another item was pressed that wasn't air
		if (!event.getCurrentItem().getType().equals(Material.AIR)) {
			int index = playerInfo.index;
			
			//if requests menu and player is seller, pick up any items
			if (menu[0].equals("request") && market.getItem(index * 45 + event.getSlot(), menu[0]).getSeller().equals(player.getUniqueId())){
				playerInfo.temp = (market.getItem(index * 45 + event.getSlot(), menu[0]));
				if (pickupItem(player)){
					MenuPainter.paintMenu(player);
					playerInfo.temp = null;
					return;
				}
			}
			
			//set the item being evaluated
			playerInfo.temp = (market.getItem(index * 45 + event.getSlot(), menu[0]));
			
			// set the player's menu
			playerInfo.menu = menu[0].concat(":buy");
			
			//set initial quantity to 1
			playerInfo.unitQuantity = 1;
			
			// paint the buy item
			MenuPainter.paintMenu(player);

			return;
		}

	}

	private static boolean checkBook(Player player, InventoryClickEvent event) {
		ItemStack book = event.getCurrentItem();
		BookMeta meta = (BookMeta)book.getItemMeta();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		
		if (meta.getTitle().toLowerCase().equals("request")){
			String page = meta.getPage(1);
			if (page.equals(null)){
				player.sendMessage("String is null.");
			}
			
			String[] fields = page.split(",");
			if (fields.length == 4){
				playerInfo.temp = new MarketSale(player.getUniqueId(), Integer.parseInt(fields[3]), Integer.parseInt(fields[2]),Integer.parseInt(fields[1]));
				playerInfo.temp.addItem(new ItemStack(Material.values()[Integer.parseInt(fields[0])]));
				player.sendMessage("Request valid.");
				return true;
			}
			player.sendMessage("Wrong number of fields.");
			
		}
		
		player.sendMessage("wrong title");
		
		return false;
	}


	/**
	 * The buy menu. Called when a player wants to buy an item
	 * 
	 * @param event
	 *            the InventoryClickEvent
	 * @param
	 */
	private static void buyMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();

		// click was made in the player's inventory
		if (event.getRawSlot() > event.getSlot()) {
			return;
		}

		// if click was to change a place in the value
		for (int i = 0; i < 9; i++) {
			int newValue;
			
			//decrement
			if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {

				newValue = (int)(playerInfo.unitQuantity - java.lang.Math.pow(10, i));
				
				// make sure the value is still positive
				if (newValue < 1) {
					newValue = 1;
				}
				
				//set quantity
				playerInfo.unitQuantity = newValue;

				// repaint
				MenuPainter.paintMenu(player);
				return;
			}
			
			//increment
			if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {

                newValue = (int) (playerInfo.unitQuantity + java.lang.Math.pow(10, i));

                //make sure there are enough of all items
                if (playerInfo.temp.getAvailiableUnits() > 0 && playerInfo.temp.getAvailiableUnits() < newValue) {
					newValue = playerInfo.temp.getAvailiableUnits();
				}


				//make sure the amount of units being purchased isn't more than the player can hold
                int max = maxUnitsPlayerCanBuy(player, playerInfo.temp);
				if (newValue > max) {
					newValue = max;
				}
				
				//set quantity
				playerInfo.unitQuantity = newValue;

				MenuPainter.paintMenu(player);
				return;
			}
		}
		
		if (event.getSlot() == MenuPainter.getLeft(inv, 3)) {
			//which menu
			if (playerInfo.menu.split(":")[0].equals("market")) {
				HelpBookCreator.buyHelp(player);
			}
			else if (playerInfo.menu.split(":")[0].equals("request")) {
				HelpBookCreator.fulfillHelp(player);
			}
			return;
			
		}

		// buy button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
			//market
			if (playerInfo.menu.split(":")[0].equals("market")) {
				try {
                    buyItem(player);
                } catch (Exception e) {
				    e.printStackTrace();
                }

				// paint the chest for the main menu
				playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
				
				MenuPainter.paintMenu(player);	
				
				//update the menu of anyone who was viewing the new change
				GUIM.updateMenu(market.getFullName(), playerInfo.menu);
				
				//rid the temp
				playerInfo.temp = null;
				
				return;
			}
			
			//request
			if (playerInfo.menu.split(":")[0].equals("request")) {
				if (fulfillItem(player)) {
					// paint the chest for the main menu
					playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
					
					MenuPainter.paintMenu(player);
					
					//rid the temp
					playerInfo.temp = null;
				}
				
				

				return;
			}
			
			//free
			if (playerInfo.menu.split(":")[0].equals("free")) {
				freeItem(player);

				// paint the chest for the main menu
				playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
				
				MenuPainter.paintMenu(player);
				
				//rid the temp
				playerInfo.temp = null;
				
				return;
			}
			

		}

		// player pressed the back button
		else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

			// paint the chest for the main menu
			playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
			playerInfo.temp = null;
			
			MenuPainter.paintMenu(player);	
			return;
		}
	}

	
	private void confirmMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();

		// click was made in the player's inventory
		if (event.getRawSlot() > event.getSlot()) {
			return;
		}

		// buy button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
			
			//make sure player has funds
			if (GUIM.economy.getBalance((OfflinePlayer)player) < (playerInfo.temp.getTotalUnitQuantity() * playerInfo.temp.getUnitPrice())){
				player.sendMessage("You don't have the funds to back this request.");
				return;
			}
			
			//remove funds
			GUIM.economy.withdrawPlayer((OfflinePlayer)player, (playerInfo.temp.getTotalUnitQuantity() * playerInfo.temp.getUnitPrice()));
			
			//place the request on the market
			GUIM.markets.get(playerInfo.currentMarket).requestedItems.add(playerInfo.temp);
			playerInfo.temp = null;
			
			// paint the chest for the main menu
			playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
			
			MenuPainter.paintMenu(player);	
			
			//update the menu of anyone who was viewing the new change
			GUIM.updateMenu(market.getFullName(), playerInfo.menu);
			
			int num = market.numSales.get(player.getUniqueId()); 
			market.numSales.remove(player.getUniqueId());
			market.numSales.put(player.getUniqueId(), num + 1);
			
			return;

		}

		// player pressed the back button
		else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

			// paint the chest for the main menu
			playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
			
			MenuPainter.paintMenu(player);	
			return;
		}
		
	}
	
	/**
	 * The sell menu. Called when a player wants to sell an item
	 * 
	 * @param event
	 *            the InventoryClickEvent
	 */
	private static void sellMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();
		
		if (event.getSlot() == MenuPainter.getLeft(inv, 3)) {
			//which menu
			HelpBookCreator.sellHelp(player);
			return;
		}

		if (!playerInfo.menu.startsWith("admin")) {
            //choose the correct stage
            switch (playerInfo.menu.split(":")[playerInfo.menu.split(":").length - 1]) {
                //First stage: players choose items and quantities
                case "quantity":
                    // create a market sale if one does not already exists
                    if (playerInfo.temp == null) {
                        playerInfo.temp = new MarketSale(player.getUniqueId());
                    }

                    // click was made in the player's inventory
                    if (event.getRawSlot() > event.getSlot()) {


                        //do nothing if the player clicked an empty spot
                        if (event.getCurrentItem().getType() == Material.AIR) {
                            return;
                        }

                        //check for the max number of items
                        if (playerInfo.temp.getItems().size() >= 9) {
                            player.sendMessage("Sale is full.");
                            return;
                        }

                        // create the representative item
                        ItemStack item = new ItemStack(event.getCurrentItem());
                        item.setAmount(1);

                        //check if that type is already part of the sale
                        for (ItemStack saleItem : playerInfo.temp.getItems()) {
                            if (saleItem.equals(item)) {
                                return;
                            }
                        }

                        //place the representative item in the sale
                        playerInfo.temp.addItem(item);

                        //make sure the previous quantity value is still valid
                        for (ItemStack saleItem : playerInfo.temp.getItems()) {
                            if (countAmount(player, saleItem) < playerInfo.temp.getTotalQuantity()) {
                                playerInfo.temp.setTotalQuantity(countAmount(player, saleItem));
                            }
                        }

                        player.sendMessage("Item added to sale.");

                        // repaint
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    // if click was to change a place in the value
                    for (int i = 0; i < 9; i++) {
                        int newValue;

                        //decrement
                        if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {

                            newValue = (int) (playerInfo.temp.getTotalQuantity() - java.lang.Math.pow(10, i));

                            // make sure the value is still positive
                            if (newValue < 1) {
                                newValue = 1;
                            }

                            //set quantity
                            playerInfo.temp.setTotalQuantity(newValue);

                            // repaint
                            MenuPainter.paintMenu(player);
                            return;
                        }

                        //increment
                        if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {

                            newValue = (int) (playerInfo.temp.getTotalQuantity() + java.lang.Math.pow(10, i));

                            //make sure there is enough of all items
                            for (ItemStack item : playerInfo.temp.getItems()) {
                                if (countAmount(player, item) < newValue) {
                                    playerInfo.temp.setTotalQuantity(countAmount(player, item));
                                    MenuPainter.paintMenu(player);
                                    return;
                                }
                            }

                            //make sure the value isn't over bounds
                            if (newValue > 2304) {
                                newValue = 2304;
                            }

                            //set quantity
                            playerInfo.temp.setTotalQuantity(newValue);

                            MenuPainter.paintMenu(player);
                            return;
                        }
                    }

                    // continue button was pressed
                    if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
                        if (playerInfo.menu.startsWith("market")) {
                            // make sure there is an item to sell
                            if (playerInfo.temp.getItems().size() == 0) {
                                return;
                            }

                            //set menuB to next sub menu
                            playerInfo.menu = playerInfo.menu.replace("quantity", "bulk");

                            player.sendMessage("Quantity set. Now, choose how many items will be sold in a bundle.");

                            //paint
                            MenuPainter.paintMenu(player);

                        } else if (playerInfo.menu.startsWith("free")) {
                            // make sure there is an item to sell
                            if (playerInfo.temp.getItems().size() == 0) {
                                return;
                            }

                            //set defaults
                            playerInfo.temp.setUnitSize(1);
                            playerInfo.temp.setUnitPrice(0);

                            //add free items
                            GUIM.markets.get(playerInfo.currentMarket).freeItems.add(playerInfo.temp);

                            //remove from inventory
                            takeItems(player, playerInfo.temp, playerInfo.temp.getTotalUnitQuantity());

                            //set menuB to next sub menu
                            playerInfo.menu = "free:view";

                            player.sendMessage("Free items added.");

                            //paint
                            MenuPainter.paintMenu(player);

                            //rid the temp
                            playerInfo.temp = null;

                        }

                        return;
                    }

                    // player pressed the back button
                    else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

                        //remove the toSell
                        playerInfo.temp = null;

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    //player clicked a possible item spot
                    else if (event.getSlot() < 18) {
                        //remove the item from the market sale
                        playerInfo.temp.removeItem(inv.getItem(event.getSlot()));

                        //repaint
                        MenuPainter.paintMenu(player);
                        return;
                    }
                    break;

                //Second stage: players choose how much of the item(s) are considered one bulk
                case "bulk":
                    // if click was to change a place in the value
                    for (int i = 0; i < 9; i++) {
                        int newValue;

                        //decrement
                        if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {

                            newValue = (int) (playerInfo.temp.getUnitSize() - java.lang.Math.pow(10, i));

                            // make sure the value is still positive
                            if (newValue < 1) {
                                newValue = 1;
                            }

                            //set bulk quantity
                            playerInfo.temp.setUnitSize(newValue);

                            // repaint
                            MenuPainter.paintMenu(player);
                            return;
                        }

                        //increment
                        if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {

                            newValue = (int) (playerInfo.temp.getUnitSize() + java.lang.Math.pow(10, i));

                            //make sure new value isn't above total item count
                            if (newValue > playerInfo.temp.getTotalQuantity()) {
                                playerInfo.temp.setUnitSize(playerInfo.temp.getTotalQuantity());
                                MenuPainter.paintMenu(player);
                                return;
                            }

                            //make sure the value isn't over bounds
							if (newValue > 2304) {
								newValue = 2304;
							}

                            //set bulk quantity
                            playerInfo.temp.setUnitSize(newValue);

                            MenuPainter.paintMenu(player);
                            return;
                        }
                    }

                    // continue button was pressed
                    if (event.getSlot() == MenuPainter.getRight(inv, 1)) {

                        //make sure the bulk value divides into the quantity
                        if (playerInfo.temp.getTotalQuantity() % playerInfo.temp.getUnitSize() != 0) {
                            player.sendMessage("Can't evenly divide the total quantity " + playerInfo.temp.getTotalQuantity() + " into units of " + playerInfo.temp.getUnitSize() + ", try a different value.");
                            return;
                        }

                        //set menuB to next sub menu
                        playerInfo.menu = playerInfo.menu.replace("bulk", "price");

                        //paint
                        MenuPainter.paintMenu(player);
                        return;
                    }

                    // player pressed the back button
                    else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.replace("bulk", "quantity");
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    break;

                //Third stage: players set price of 1 bulk
                case "price":
                    // if click was to change a place in the value
                    for (int i = 0; i < 9; i++) {
                        int newValue;

                        //decrement
                        if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {

                            newValue = (int) (playerInfo.temp.getUnitPrice() - java.lang.Math.pow(10, i));

                            // make sure the value is still positive
                            if (newValue < 1) {
                                newValue = 1;
                            }

                            //set price
                            playerInfo.temp.setUnitPrice(newValue);

                            // repaint
                            MenuPainter.paintMenu(player);
                            return;
                        }

                        //increment
                        if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {

                            newValue = (int) (playerInfo.temp.getUnitPrice() + java.lang.Math.pow(10, i));

                            //make sure the value isn't over bounds
							if (newValue > 2304) {
								newValue = 2304;
							}

                            //set price
                            playerInfo.temp.setUnitPrice(newValue);

                            MenuPainter.paintMenu(player);
                            return;
                        }
                    }

                    // continue button was pressed
                    if (event.getSlot() == MenuPainter.getRight(inv, 1)) {

                        listMarketSale(player);

                        playerInfo.temp = null;

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
                        MenuPainter.paintMenu(player);

                        //update the menu of anyone who was viewing the new change
                        GUIM.updateMenu(market.getFullName(), playerInfo.menu);

                        //rid the temp
                        playerInfo.temp = null;
                        return;
                    }

                    // player pressed the back button
                    else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.replace("price", "bulk");
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    break;
            }
        } else {
            //choose the correct stage (3 stages)
            switch (playerInfo.menu.split(":")[playerInfo.menu.split(":").length - 1]) {
                //First stage: players choose items and quantities
                case "quantity":
                    // create a server market sale if one does not already exists
                    if (playerInfo.temp == null) {
                        playerInfo.temp = new MarketSale(null);
                    }

                    // click was made in the player's inventory
                    if (event.getRawSlot() > event.getSlot()) {

                        //do nothing if the player clicked an empty spot
                        if (event.getCurrentItem().getType() == Material.AIR) {
                            return;
                        }

                        //check for the max number of items
                        if (playerInfo.temp.getItems().size() >= 9) {
                            player.sendMessage("Sale is full.");
                            return;
                        }

                        // create the representative item
                        ItemStack item = new ItemStack(event.getCurrentItem());
                        item.setAmount(1);

                        //check if that type is already part of the sale
                        for (ItemStack saleItem : playerInfo.temp.getItems()) {
                            if (saleItem.equals(item)) {
                                return;
                            }
                        }

                        //place the representative item in the sale
                        playerInfo.temp.addItem(item);

                        player.sendMessage("Item added to sale.");

                        // repaint
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    // continue button was pressed
                    if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
                        // make sure there is an item to sell
                        if (playerInfo.temp.getItems().size() == 0) {
                            return;
                        }

                        //set menuB to next sub menu
                        playerInfo.menu = playerInfo.menu.replace("quantity", "bulk");

                        player.sendMessage("Choose how many items will be sold in a bundle.");

                        //paint
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    // player pressed the back button
                    else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

                        //remove the toSell
                        playerInfo.temp = null;

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    //player clicked a possible item spot
                    else if (event.getSlot() < 18) {
                        //remove the item from the market sale
                        playerInfo.temp.removeItem(inv.getItem(event.getSlot()));

                        //repaint
                        MenuPainter.paintMenu(player);

                        return;
                    }
                    break;

                //Second stage: players choose how much of the item(s) are considered one bulk
                case "bulk":
                    // if click was to change a place in the value
                    for (int i = 0; i < 9; i++) {
                        int newValue;

                        //decrement
                        if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {

                            newValue = (int)(playerInfo.temp.getUnitSize() - java.lang.Math.pow(10, i));

                            // make sure the value is still positive
                            if (newValue < 1) {
                                newValue = 1;
                            }

                            // set bulk quantity
                            playerInfo.temp.setUnitSize(newValue);

                            // repaint
                            MenuPainter.paintMenu(player);
                            return;
                        }

                        //increment
                        if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {

                            newValue = (int)(playerInfo.temp.getUnitSize() + java.lang.Math.pow(10, i));

                            //make sure the value isn't over bounds
                            if (newValue > 2304) {
                                newValue = 2304;
                            }

                            //set bulk quantity
                            playerInfo.temp.setUnitSize(newValue);

                            MenuPainter.paintMenu(player);
                            return;
                        }
                    }

                    //continue button was pressed
                    if (event.getSlot() == MenuPainter.getRight(inv, 1)) {
                        //set menu to next sub-menu
                        playerInfo.menu = playerInfo.menu.replace("bulk", "price");

                        //paint
                        MenuPainter.paintMenu(player);
                        return;
                    }

                    //player pressed the back button
                    else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.replace("bulk", "quantity");
                        MenuPainter.paintMenu(player);

                        return;
                    }

                    break;

                //Third stage: players set price of 1 bulk unit
                case "price":
                    // if click was to change a place in the value
                    for (int i = 0; i < 9; i++) {
                        int newValue;

                        //decrement
                        if (event.getSlot() == MenuPainter.getRight(inv, (18 + i))) {

                            newValue = (int)(playerInfo.temp.getUnitPrice() - java.lang.Math.pow(10, i));

                            // make sure the value is not less than 0
                            if (newValue < 0) {
                                newValue = 0;
                            }

                            //set price
                            playerInfo.temp.setUnitPrice(newValue);

                            // repaint
                            MenuPainter.paintMenu(player);
                            return;
                        }

                        //increment
                        if (event.getSlot() == MenuPainter.getRight(inv, (27 + i))) {

                            newValue = (int)(playerInfo.temp.getUnitPrice() + java.lang.Math.pow(10, i));

                            //make sure the price isn't over bounds
                            if (newValue > 999999999) {
                                newValue = 999999999;
                            }

                            //set price
                            playerInfo.temp.setUnitPrice(newValue);

                            MenuPainter.paintMenu(player);
                            return;
                        }
                    }

                    // continue button was pressed
                    if (event.getSlot() == MenuPainter.getRight(inv, 1)) {

                        listServerMarketSale(player);

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.split(":")[0].concat(":view");
                        MenuPainter.paintMenu(player);

                        //update the menu of anyone who was viewing the new change
                        GUIM.updateMenu(market.getFullName(), playerInfo.menu);

                        //rid the temp
                        playerInfo.temp = null;
                        return;
                    }

                    // player pressed the back button
                    else if (event.getSlot() == MenuPainter.getRight(inv, 0)) {

                        // paint the chest for the main menu
                        playerInfo.menu = playerInfo.menu.replace("price", "bulk");
                        MenuPainter.paintMenu(player);

                        return;
                    }
                    break;
            }
        }
	}

	private static boolean listMarketSale(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		
		//list the sale on the market
		MarketSale sale = playerInfo.temp;
		for (ItemStack item : sale.getItems()){
			if (!player.getInventory().containsAtLeast(item, sale.getTotalQuantity())){
				player.sendMessage("SALE ABORTED. You did not have enough items in your inventory.");
				playerInfo.temp = null;
				return false;
			}
		}

		takeItems(player, playerInfo.temp, playerInfo.temp.getTotalUnitQuantity());
		
		GUIM.markets.get(playerInfo.currentMarket).marketItems.add(sale);
		
		Market market = GUIM.markets.get(playerInfo.currentMarket);
		int num = market.numSales.get(player.getUniqueId()); 
		market.incrementPlayer(player.getUniqueId());
		return true;

	}

	private static boolean listServerMarketSale(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());

		GUIM.markets.get(playerInfo.currentMarket).marketItems.add(playerInfo.temp);
		return true;
	}

    private static boolean listGlobalMarketSale(Player player) {
        PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());

        Market.globalMarketItems.add(playerInfo.temp);
        return true;
    }

	private static int countAmount(Player player, ItemStack item) {
		int i = 0;
		for (ItemStack invItem : player.getInventory().getContents()){
			if (invItem != null){
				ItemStack item1 = new ItemStack(invItem);
				ItemStack item2 = new ItemStack(item);
				item1.setAmount(1);
				item2.setAmount(1);
				if (item1.equals(item2)){
					i += invItem.getAmount();
				}
			}
		}
		return i;
	}

	private void optionsMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player)event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();
		
		// if back button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
			// set chest up for item menu
			
			// set the player's menu
			playerInfo.menu = "main";
			
			// paint the sell menu
			MenuPainter.paintMenu(player);
			return;
		}
	}
	
	private void adminMenuEvent(InventoryClickEvent event, Market market) {
		Player player = (Player) event.getWhoClicked();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = event.getInventory();
		
		// if op is selling infinite item from the server
		if (event.getSlot() == MenuPainter.getLeft(inv, 0)) {
			// set the player's menu
			playerInfo.menu = "admin:sellserver:quantity";
			
			// paint the sell menu
			MenuPainter.paintMenu(player);
			return;
		}
		
		// if back button was pressed
		if (event.getSlot() == MenuPainter.getRight(inv, 0)) {
			// set chest up for item menu
			
			// set the player's menu
			playerInfo.menu = "main";
			
			// paint the sell menu
			MenuPainter.paintMenu(player);
			return;
		}
	}

	/**
	 * Player buys an item. Item is taken off of the market
	 * 
	 * @param player
	 *            the player buying the item
	 */
	private static void buyItem(Player player) throws Exception {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Market market = GUIM.markets.get(playerInfo.currentMarket);
		
		if (market == null) {
			return;
		}

		// make sure the item wasn't quickly bought buy someone else
		if (playerInfo.temp == null) {
			player.sendMessage("I'm sorry, but this item has already been sold.");
			return;
		}

		// make sure the item wasn't quickly bought buy someone else
		if (playerInfo.temp.getSeller() != null && playerInfo.temp.getTotalUnitQuantity() < playerInfo.unitQuantity) {
			player.sendMessage("I'm sorry, but this was just sold.");
			return;
		}
		
		// if the player has room and can afford the item, buy it
		if (!playerHasSpace(player, playerInfo.temp, playerInfo.unitQuantity)) {
			player.sendMessage("I'm sorry, but you don't have enough space.");
			return;
		}

		if ((!playerCanAfford(player, playerInfo.temp, playerInfo.unitQuantity))
				&& (!player.getUniqueId().equals(playerInfo.temp.getSeller()))) {
			player.sendMessage("You can't afford that.");
			return;
		}

		//buy the item(s)

		//if we're not buying from the server's infinite supply
		if (playerInfo.temp.getSeller() != null) {
            //give the seller the money
            GUIM.economy.depositPlayer(Bukkit.getOfflinePlayer(playerInfo.temp.getSeller()), playerInfo.unitQuantity * playerInfo.temp.getUnitPrice());
            OfflinePlayer seller = Bukkit.getOfflinePlayer(playerInfo.temp.getSeller());

            if (seller.isOnline()) {
                Player onlineSeller = Bukkit.getPlayer(playerInfo.temp.getSeller());

                TextComponent saleMsg = new TextComponent(player.getName() + " just bought from one of your sales. Hover over this message to see sale info.");
                saleMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playerInfo.temp.getSaleInfo(playerInfo, true)).create()));

                onlineSeller.spigot().sendMessage(saleMsg);
            }

            //remove items from the sale
            playerInfo.temp.setTotalQuantity(playerInfo.temp.getTotalQuantity() - (playerInfo.unitQuantity * playerInfo.temp.getUnitSize()));

            // remove the sale if it is depleted
            if (playerInfo.temp.getTotalQuantity() <= 0) {
                for (MarketSale item: market.marketItems){
                    if (item.equals(playerInfo.temp)){
                        market.decrementPlayer(playerInfo.temp.getSeller());
                        market.marketItems.remove(item);

                        break;
                    }
                }
            }
        }

        //add the items to the player's inventory
        giveItems(player, playerInfo.temp, playerInfo.unitQuantity);
				
		//withdraw money from the purchaser
		GUIM.economy.withdrawPlayer(player, playerInfo.unitQuantity * playerInfo.temp.getUnitPrice());

        TextComponent purchaseMsg = new TextComponent("Purchase successful. Hover over this message to see sale information.");
        purchaseMsg.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(playerInfo.temp.getSaleInfo(playerInfo, false)).create()));

        player.spigot().sendMessage(purchaseMsg);
	}
	
	private static boolean fulfillItem(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		MarketSale marketSale = playerInfo.temp;	
		
		//check if there is anything to fulfill
		if (marketSale.getAvailiableUnits() <= 0){
			player.sendMessage("This request is complete.");
			return false;
		}
		
		//check if player has items
		if (!playerHasItems(player, marketSale, playerInfo.unitQuantity)){
			player.sendMessage("You do not have the items in your inventory.");
			return false;
		}
		
		//remove items from player
		takeItems(player, marketSale, playerInfo.unitQuantity);
		
		//give the player the reward
		GUIM.economy.depositPlayer(player, playerInfo.unitQuantity * marketSale.getUnitPrice());
		player.sendMessage("You have been rewarded $" + playerInfo.unitQuantity * marketSale.getUnitSize());
		
		//add to the sale
		marketSale.setFulfilled(marketSale.getFulfilled() + playerInfo.unitQuantity);
		
		//notify the seller if online
		
		return true;
	}
	
	private static boolean pickupItem(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Market market = GUIM.markets.get(playerInfo.currentMarket);
		MarketSale marketSale = playerInfo.temp;	
		
		//check if request has been fulfilled
		if (marketSale.getFulfilled() <= 0){
			return false;
		}
		
		//make sure player has room to pick up items
		int bulks = marketSale.getFulfilled();
		while (!playerHasSpace(player, marketSale, bulks)){
			bulks--;
			if (bulks <= 0){
				player.sendMessage("Not enough room to pick up items");
				return false;
			}
		}
			
		//give items to player
		giveItems(player, marketSale, bulks);
		
		//remove from the sale
		marketSale.setPickedUp(marketSale.getPickedUp() + bulks);
		marketSale.setFulfilled(marketSale.getFulfilled() - bulks);
		
		player.sendMessage("You picked up " + bulks + " units of your request");
		
		//check if the sale is done and picked up
		if (marketSale.getPickedUp() == marketSale.getTotalUnitQuantity()){
			//remove the sale
			market.decrementPlayer(marketSale.getSeller());
			market.requestedItems.remove(marketSale);
			GUIM.updateMenu(playerInfo.marketName, "request:view");
			
			return true;
		}
		
		
		return false;
	}


	private static boolean freeItem(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Market market = GUIM.markets.get(playerInfo.currentMarket);
		MarketSale marketSale = playerInfo.temp;
		
		//check if player has room
		if (!playerHasSpace(player, marketSale, playerInfo.unitQuantity)){
			player.sendMessage("You do not have enough space in your inventory.");
			return false;
		}
		
		//remove items from market
		marketSale.setTotalQuantity(marketSale.getUnitSize() * (marketSale.getTotalUnitQuantity() - playerInfo.unitQuantity));
		if (marketSale.getTotalQuantity() <= 0){
			market.freeItems.remove(marketSale);
			GUIM.updateMenu(market.getFullName(), "free:view");
		}
		
		//place items
		return giveItems(player, marketSale, playerInfo.unitQuantity);
	}


    private static boolean giveItems(Player player, MarketSale marketSale, int bulkAmount) {
        Inventory inv = player.getInventory();
        int i;
        ItemStack newItem;

        //add items
        //for each item in the sale
        for (ItemStack stack : marketSale.getItems()) {

            i = marketSale.getUnitSize() * bulkAmount;

            int j = 0;
            //iterate though the player's inventory
            for (ItemStack invStack : inv.getContents()) {
                //if there is a stack
                if (invStack != null){
                    //if the items are similar
                    if (stack.isSimilar(invStack)) {
                        //finish off this stack
                        if (i + invStack.getAmount() <= stack.getMaxStackSize()) {
                            newItem = new ItemStack(invStack);
                            newItem.setAmount(i + invStack.getAmount());

                            //Remove the market info lore from earlier
                            ItemMeta newInfo = newItem.getItemMeta();
                            newInfo.setLore(null);
                            newItem.setItemMeta(newInfo);

                            inv.setItem(j, null);
                            inv.addItem(newItem);
                            i = 0;
                        } else {
                            //add the whole stack and keep going
                            i -= stack.getMaxStackSize() - invStack.getAmount();
                            invStack.setAmount(stack.getMaxStackSize());
                            newItem = new ItemStack(invStack);

                            //Remove the market info lore from earlier
                            ItemMeta newInfo = newItem.getItemMeta();
                            newInfo.setLore(null);
                            newItem.setItemMeta(newInfo);

                            inv.setItem(j, null);
                            inv.addItem(newItem);
                        }
                    }
                } else { //if there is not a stack
                    //finish off this stack
                    if (i <= stack.getMaxStackSize()) {
                        //player.sendMessage(item.getData().getItemType().getSaleInfo() + " x " + item.getAmount());
                        newItem = new ItemStack(stack);
                        newItem.setAmount(i);

                        //Remove the market info lore from earlier
                        ItemMeta newInfo = newItem.getItemMeta();
                        newInfo.setLore(null);
                        newItem.setItemMeta(newInfo);

                        //player.sendMessage(newItem.getData().getItemType().getSaleInfo() + " x " + newItem.getAmount());
                        inv.addItem(newItem);
                        i = 0;
                    } else { //add the whole stack and keep going
                        i -= stack.getMaxStackSize();
                        newItem = new ItemStack(stack);
                        newItem.setAmount(stack.getMaxStackSize());

                        //Remove the market info lore from earlier
                        ItemMeta newInfo = newItem.getItemMeta();
                        newInfo.setLore(null);
                        newItem.setItemMeta(newInfo);

                        inv.addItem(newItem);
                    }
                }

                if (i == 0) {
                    //finish this item
                    break;
                }
                j++;
            }
            //something went wrong
            if (i != 0) {
                return false;
            }

        }
        //all items have been added
        return true;
    }


	/**
	 * Returns whether or not a player can afford an item
	 * 
	 * @param player
	 *            The player
	 * @param item
	 *            The item the player wants to buy
	 * @param quantity 
	 * @return whether or not a player can afford an item
	 */
	private static boolean playerCanAfford(Player player, MarketSale item, int quantity) {
		
		double money = GUIM.economy.getBalance(player);

		if (money >= (item.getUnitPrice() * quantity)) {
			return true;
		}

		return false;
	}

	/**
	 * Returns whether or not a player has enough inventory space
	 * 
	 * @param player
	 *            The player
	 * @return whether or not a player has enough inventory space for the given number of units
	 */
	private static boolean playerHasSpace(Player player, MarketSale marketSale, int unitsToBuy) {
	    int i = 0;
		for (ItemStack stack : marketSale.getItems()) {
			i += marketSale.getUnitSize() * unitsToBuy;
			for (ItemStack invStack : player.getInventory().getContents()) {
				if (invStack == null) {
					i -= stack.getMaxStackSize();
				} else if (invStack.isSimilar(stack)) {
					i -= (stack.getMaxStackSize() - invStack.getAmount());
				}
			}

			if (i > 0) {
				return false;
			}
		}
		return true;
	}

	private static int maxUnitsPlayerCanBuy(Player player, MarketSale marketSale)
    {
        int units = 0;
        while (playerHasSpace(player, marketSale, units)) {
            units++;
        }

        return (units - 1);
    }
	
	private static boolean playerHasItems(Player player, MarketSale marketSale, int amount) {
		int i;
		//for each item in the marketsale
		for (ItemStack item : marketSale.getItems()) {
			//calculate the correct amount of items
			i = marketSale.getUnitSize() * amount;
			//iterate through the player's inventory
			for (ItemStack invItem: player.getInventory().getContents()) {
				//if similar item
				if (item.isSimilar(invItem)) {
					//subtract what the player has
					i -= invItem.getAmount();
				}
			}
			//if we still needed items, return false.
			if (i > 0) {
				return false;
			}
		}
		//all items are accounted for. return true
		return true;
	}
	
	/**
	 * Takes the items specified in marketSale away from the players inventory, in bulkAmount bulks
	 * @param player The player who is having items taken away
	 * @param marketSale The sale which contains which items to take away from the player
	 * @param bulkAmount The number of bulk sales to take away from the player
	 * @return true if successful, false if unsuccessful
	 */
	private static boolean takeItems(Player player, MarketSale marketSale, int bulkAmount) {
		Inventory inv = player.getInventory();
		int i;
		ItemStack newItem;
		
		//take items
		//for each item in the sale
		for (ItemStack item: marketSale.getItems()) {
			i = marketSale.getUnitSize() * bulkAmount;
			int j = 0;
			//iterate though the player's inventory
			for (ItemStack stack : inv.getContents()) {
				//if there is a stack
				if (stack != null) {
					//if the items are similar
					if (item.isSimilar(stack)) {
						//if we do not need to take the whole stack
						if (i < stack.getAmount()) {
							
							stack.setAmount(stack.getAmount() - i);
							newItem = new ItemStack(stack);
							inv.setItem(j, null);
							inv.addItem(newItem);
							i -= i;
						}
						//remove the whole stack and keep going
						else{
							i -= stack.getAmount();
							inv.setItem(j, null);
						}
					}
				}
				
				if (i == 0) {
					//finish this item
					break;
				}
				j++;
			}
			//something went wrong
			if (i != 0) {
				return false;
			}
			
		}
		//all items have been added
		return true;
	}


	public void setupMarket(Player player, String name) {
		
		//make sure player has permission
		if (!player.hasPermission("GUIM.create")){
			player.sendMessage("Sorry, you do not have permission to do that.");
			return;
		}
		
		//make sure that marketname is not already in use
		if (GUIM.markets.get(player.getUniqueId() + ":" + name) != null) {
			player.sendMessage("You already have a market with that name.");
			return;
		}
		
		player.sendMessage("Please right-click the block which you would like to make into a market.");
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		if (playerInfo == null) {
			GUIM.addPlayerInfo(player.getUniqueId());
			playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		}
		
		playerInfo.creatingMarket = true;
		playerInfo.marketName = name;
		
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void setupMarket(PlayerInteractEvent event) {
		//get the player and the PlayerInfo
		Player player = event.getPlayer();
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		
		
		if (playerInfo == null) {
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (playerInfo.creatingMarket == true) {
				event.setCancelled(true);
				HashSet<Location> locations = new HashSet<Location>();
				locations.add(event.getClickedBlock().getLocation());
				
				Market m = new Market(player.getUniqueId(), playerInfo.marketName, locations, new HashMap<UUID, Integer>(), plugin);
				GUIM.markets.put(m.getFullName(), m);
				GUIM.updateMarketLocations();

				playerInfo.creatingMarket = false;
				playerInfo.marketName = null;
				playerInfo.accessBlock = null;
				player.sendMessage("Market has been created. Right-Click the market to access");
			}
		}
		

	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void preventBlocks(org.bukkit.event.entity.EntityExplodeEvent event) {
		for (Block block: event.blockList()) {
			if (GUIM.getMarketLocations().containsKey(block.getLocation())) {
				event.blockList().remove(block);
			}
		}
	}
}
