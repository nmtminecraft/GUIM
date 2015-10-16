package com.m0pt0pmatt.GUIM;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.m0pt0pmatt.GUIM.Player.PlayerInfo;

/**
 * This is where the actual display items and buttons in the menu are drawn.
 * @author Matthew, James
 */

public class MenuPainter {
	
	public static final int Cancel = 0, 
			Accept = 1, 
			Help = 2, 
			Options = 3, 
			Left = 4, 
			Right = 5, 
			MarketItems = 6,
			BuyItem = 7,
			SellOrRequest = 8, 
			FreeItems = 9,
			Admin = 10;
	
	/**
	 * Returns a place in the inventory, right-aligned to the offset
	 * @param inv 
	 * 			 	the Inventory to be examined
	 * @param offset
	 * 				the offset after the alignment
	 * @return a place in the inventory, right-aligned to the offset
	 */
	public static int getRight(Inventory inv, int offset) {
		return inv.getSize() - 1 - offset;
	}

	/**
	 * Returns a place in the inventory, left-aligned to the offset
	 * 
	 * @param inv
	 *            the Inventory to be examined
	 * @param offset
	 *            the offset after the alignment
	 * @return a place in the inventory, left-aligned to the offset
	 */
	public static int getLeft(Inventory inv, int offset) {
		return inv.getSize() - 9 + offset;
	}
	
	private static void paintMainMenu(Player player) {
		Inventory inv = GUIM.getPlayerInfo(player.getUniqueId()).inventory;
		
		inv.clear();
		inv.setItem(getLeft(inv, 0), nameItem(getButton(MarketItems), "Market Items"));
		inv.setItem(getLeft(inv, 1), nameItem(getButton(SellOrRequest), "Requested Items"));
		inv.setItem(getLeft(inv, 2), nameItem(getButton(FreeItems), "Free Items"));
		inv.setItem(getLeft(inv, 3), nameItem(getButton(Options), "Options"));
		inv.setItem(getLeft(inv, 4), nameItem(getButton(Admin), "Admin Menu"));
		inv.setItem(getLeft(inv, 5), nameItem(getButton(Help), "Help"));
	}
	
	private static void paintViewItemMenu(Player player) {

		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;
		
		// clear inventory
		inv.clear();	

		// add menu buttons
		// add next buttons
		inv.setItem(getLeft(inv, 0), nameItem(getButton(Left), "Scroll Left"));
		inv.setItem(getRight(inv, 0), nameItem(getButton(Right), "Scroll Right"));
		inv.setItem(getRight(inv, 2), nameItem(getButton(Cancel), "Back"));
		inv.setItem(getLeft(inv, 3), nameItem(getButton(Help), "Help"));

		// get which list of items and setup custom buttons
		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
		ArrayList<MarketSale> items;
		switch(playerInfo.menu.split(":")[0]){
		case "market":
			items = market.marketItems;
			inv.setItem(getRight(inv, 1), nameItem(getButton(SellOrRequest), "Sell Items"));
			break;
		case "request":
			items = market.requestedItems;
			break;	
		case "free":
			items = market.freeItems;
			inv.setItem(getRight(inv, 1), nameItem(getButton(SellOrRequest), "Add Items"));
			break;
		default:
			items = market.marketItems;
			break;
		}
		
		//check if the index is in bounds
		if (playerInfo.index * 45 >= items.size()){
			return;
		}
		
		//display items
		for (int j = 0; j < java.lang.Math.min((inv.getSize() - 9), items.size() - (playerInfo.index * 45)); j++) {
			ItemStack item = items.get((playerInfo.index * 45) + j).getItems().getFirst();
			List<String> saleInfo = new ArrayList<String>();
			MarketSale currentItem = market.getItem(playerInfo.index * 45 + j, playerInfo.menu.split(":")[0]);
			
			saleInfo.add("Seller: " + Bukkit.getOfflinePlayer(currentItem.getSeller()).getName());
			if (currentItem.getNumPerUnits() > 1) {
				saleInfo.add("Units per bundle: " + currentItem.getNumPerUnits());
				saleInfo.add("Price per bundle: $" + currentItem.getUnitPrice());
				saleInfo.add("Bundles remaining: " + currentItem.getUnitQuantity());
			} else {
				saleInfo.add("Price per unit: $" + currentItem.getUnitPrice());
				saleInfo.add("Units remaining: " + currentItem.getTotalQuantity());
			}
			ItemMeta info = item.getItemMeta();
			info.setLore(saleInfo);
			item.setItemMeta(info);
			inv.setItem(j, item);
		}

	}
	
	private static void paintBuyMenu(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;

		// clear inventory
		inv.clear();
		
		// add menu buttons
		// add buy button
		inv.setItem(getRight(inv, 1), nameItem(getButton(Accept), "Purchase Item(s)"));

		// add back button
		inv.setItem(getRight(inv, 0), nameItem(getButton(Cancel), "Go Back"));
		inv.setItem(getLeft(inv, 3), nameItem(getButton(Help), "Help"));
		
		//add price button
		inv.setItem(getRight(inv, 36), nameItem(getButton(FreeItems), "Sale is $" + playerInfo.temp.getUnitPrice() + " per unit"));
		inv.setItem(getRight(inv, 37), nameItem(getButton(FreeItems), playerInfo.temp.getAvailiableUnits() + " bulks left"));
		inv.setItem(getRight(inv, 38), nameItem(getButton(FreeItems), "Seller: " + Bukkit.getOfflinePlayer(playerInfo.temp.getSeller()).getName()));
		
		// add items that are being sold
		int i = 0;
		for (ItemStack item : playerInfo.temp.getItems()) {
			inv.setItem(i, item);
			inv.getItem(i).setAmount(playerInfo.temp.getNumPerUnits());
			i++;
		}
		
		//add increment and decrement buttons
		for (i = 0; i < 9; i++){
			inv.setItem(getRight(inv, 27 + i), nameItem(getButton(Right), "Increment"));
			inv.setItem(getRight(inv, 18 + i), nameItem(getButton(Left), "Decrement"));
		}

		// add price
		int price = playerInfo.unitQuantity;
		for (i = 0; i < 9; i++) {
			int j = 0;
			while ((price % (java.lang.Math.pow(10, i + 1))) != 0) {
				j++;
				price -= java.lang.Math.pow(10, i);
			}
			
			// j = 0. make a zero
			if (j == 0) {
				
			} else {
				// j is not zero. make sticks
				inv.setItem(getRight(inv, 9 + i), (new ItemStack(Material.STICK, j)));
			}
		}

		
	}
	
	private static void paintConfirmMenu(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;

		// clear inventory
		inv.clear();
		
		// add menu buttons
		// add buy button
		inv.setItem(getRight(inv, 1), nameItem(getButton(Accept), "Ask for these items"));

		// add back button
		inv.setItem(getRight(inv, 0), nameItem(getButton(Cancel), "Go Back"));
		
		//add price button
		inv.setItem(getRight(inv, 36), nameItem(getButton(FreeItems), "Sale is $" + playerInfo.temp.getUnitPrice() + " per unit"));
		inv.setItem(getRight(inv, 37), nameItem(getButton(FreeItems), playerInfo.temp.getUnitQuantity() + " bulks total"));
		inv.setItem(getRight(inv, 38), nameItem(getButton(FreeItems), "Seller: " + playerInfo.temp.getSeller()));
		
		// add items
		int i = 0;
		for (ItemStack item: playerInfo.temp.getItems()){
			inv.setItem(i, item);
			inv.getItem(i).setAmount(playerInfo.temp.getNumPerUnits());
			i++;
		}
		
	}
	
	private static void paintSellMenu(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;

		// clear inventory
		inv.clear();

		// add menu buttons
		// add buy button
		inv.setItem(getRight(inv, 1), nameItem(getButton(Accept), "Go Forward"));
		inv.setItem(getLeft(inv, 3), nameItem(getButton(Help), "Help"));

		// add back button
		inv.setItem(getRight(inv, 0), nameItem(getButton(Cancel), "Go Back"));

		//add increment and decrement buttons
		for (int i = 0; i < 9; i++){
			inv.setItem(getRight(inv, 27 + i), nameItem(getButton(Right), "Increment"));
			inv.setItem(getRight(inv, 18 + i), nameItem(getButton(Left), "Decrement"));
		}
		
		// add the item if it exists
		if (playerInfo.temp == null){
			playerInfo.temp = new MarketSale(player.getUniqueId());
		}
		int i = 0;
		for (ItemStack item: playerInfo.temp.getItems()) {
			inv.setItem(i, item);
			i++;
		}
		
		//finish if there is not a price (item is free)
		if (playerInfo.menu.split(":")[1].equals("free")){
			return;
		}
		
		
		// add price
		int amount = 0;
		String stage = playerInfo.menu.split(":")[2];
		switch(stage){
		case "quantity":
			amount = playerInfo.temp.getUnitQuantity();
			break;
		case "bulk":
			amount = playerInfo.temp.getNumPerUnits();
			break;
		case "price":
			amount = playerInfo.temp.getUnitPrice();
			break;
		}
		
		for (i = 0; i < 9; i++) {
			int j = 0;
			while ((amount % (java.lang.Math.pow(10, i + 1))) != 0) {
				j++;
				amount -= java.lang.Math.pow(10, i);
			}
			// j = 0. make a zero
			if (j == 0) {

			}
			// j is not zero. make sticks
			else {
				inv.setItem(getRight(inv, 9 + i), new ItemStack(Material.STICK, j));
			}
		}
	}
	
	
	
	private static void paintOptionsMenu(Player player){
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;

		// clear inventory
		inv.clear();

		// add menu buttons

		// add back button
		inv.setItem(getRight(inv, 0), nameItem(getButton(Cancel), "Go Back"));
	}
	
	/**
	 * paints the admin menu.
	 * @param player
	 */
	private static void paintAdminMenu(Player player){
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;

		// clear inventory
		inv.clear();

		// add menu buttons

		// add back button
		inv.setItem(getRight(inv, 0), nameItem(getButton(Cancel), "Go Back"));
	}

	/**
	 * Paints the chest correctly, depending on the players current menu.
	 * Eventually, this will act as the sole interface for painting menus.
	 * @param player
	 */
	public static void paintMenu(Player player) {
		if (player == null){
			System.out.println("1");
			return;
		}
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		if (playerInfo == null){
			System.out.println("2");
			return;
		}
		String menu = playerInfo.menu;
		if (menu == null){
			System.out.println("3");
			return;
		}
		
		Market market = GUIM.marketNames.get(playerInfo.currentMarket);
		if (market == null){
			System.out.println("4");
			return;
		}
		
		String[] parts = menu.split(":");
		
		switch (parts[0]){
		case "main":
			paintMainMenu(player);
			break;
		case "market":
			switch (parts[1]){
			case "view": 
				paintViewItemMenu(player);
				break;
			case "buy":
				paintBuyMenu(player);
				break;
			case "sell":
				paintSellMenu(player);
				break;
			}
			break;
		case "request":
			switch (parts[1]){
			case "view":
				paintViewItemMenu(player);
				break;
			case "buy":
				paintBuyMenu(player);
				break;
			case "confirm":
				paintConfirmMenu(player);
				break;
			}
			break;
		case "free":
			switch (parts[1]){
			case "view":
				paintViewItemMenu(player);
				break;
			case "buy":
				paintTakeMenu(player);
				break;
			case "sell":
				paintSellMenu(player);
				break;
			}
			break;
		case "options":
			paintOptionsMenu(player);
			break;
		case "admin":
			paintAdminMenu(player);
			break;
		}
		
	}
	
	private static void paintTakeMenu(Player player) {
		PlayerInfo playerInfo = GUIM.getPlayerInfo(player.getUniqueId());
		Inventory inv = playerInfo.inventory;

		// clear inventory
		inv.clear();
		
		// add menu buttons
		// add buy button
		inv.setItem(getRight(inv, 1), nameItem(getButton(Accept), "Take these items"));

		// add back button
		inv.setItem(getRight(inv, 0), nameItem(getButton(Cancel), "Go Back"));
		
		//add price button
		inv.setItem(getRight(inv, 37), nameItem(getButton(FreeItems), playerInfo.temp.getAvailiableUnits() + " bulks left"));
		inv.setItem(getRight(inv, 38), nameItem(getButton(FreeItems), "Seller: " + playerInfo.temp.getSeller()));
		
		// add items
		int i = 0;
		for (ItemStack item: playerInfo.temp.getItems()){
			inv.setItem(i, item);
			inv.getItem(i).setAmount(playerInfo.temp.getNumPerUnits());
			i++;
		}
		
		//add increment and decrement buttons
		for (i = 0; i < 9; i++){
			inv.setItem(getRight(inv, 27 + i), nameItem(getButton(Right), "Increment"));
			inv.setItem(getRight(inv, 18 + i), nameItem(getButton(Left), "Decrement"));
		}

		// add price
		int price = playerInfo.unitQuantity;
		for (i = 0; i < 9; i++) {
			int j = 0;
			while ((price % (java.lang.Math.pow(10, i + 1))) != 0) {
				j++;
				price -= java.lang.Math.pow(10, i);
			}
			// j = 0. make a zero
			if (j == 0) {

			}
			// j is not zero. make sticks
			else {
				inv.setItem(getRight(inv, 9 + i), (new ItemStack(Material.STICK, j)));
			}
		}
		
	}

	

	/**
	 * This method is a placeholder for when buttons can be configurable itemstacks. For now, it just returns a block of wool in the color you specify
	 * @param whichButton
	 * @return
	 */
	private static ItemStack getButton(int whichButton) {
		switch (whichButton) {
			case Cancel:
				return new ItemStack(Material.BARRIER);
			case Accept:
				return new ItemStack(Material.SLIME_BLOCK);
			case Help: 
				return new ItemStack(Material.BOOK);
			case Options: 
				return new ItemStack(Material.IRON_PICKAXE);
			case Admin: 
				return new ItemStack(Material.PISTON_BASE);
			case Left: 
				return new ItemStack(Material.TORCH);
			case Right:
				//ItemStack result = new ItemStack(Material.REDSTONE_LAMP_OFF);
				//result.getData().
				//return result;
				return new ItemStack(Material.REDSTONE_TORCH_ON);
			case MarketItems: 
				return new ItemStack(Material.ANVIL);
			case FreeItems: 
				return new ItemStack(Material.CHEST);
			case BuyItem: 
				return new ItemStack(Material.IRON_AXE);
			case SellOrRequest: 
				return new ItemStack(Material.PAPER);
		}
		return new ItemStack(Material.WOOL, 1, (byte) whichButton);
	}
	
	private static ItemStack nameItem(ItemStack item, String name){
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
}
