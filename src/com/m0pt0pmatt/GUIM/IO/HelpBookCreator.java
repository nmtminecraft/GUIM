package com.m0pt0pmatt.GUIM.IO;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

/**
 * Handles giving players help books to assist them with each area of the Market.
 * @author Matthew, James
 */
public class HelpBookCreator {

	private static boolean hasRoom(Inventory inv){
		if (inv.firstEmpty() == -1){
			return false;
		}
		return true;
	}
	
	
	public static void mainMenuHelp(Player player){
		//make sure player has room
		if (!hasRoom(player.getInventory())){
			player.sendMessage("Clear a spot in your inventory to see the help manual for this menu.");
			return;
		}
		
		//create the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		
		meta.setTitle("GUIM Help: Main Menu");
		meta.setAuthor("");
		
		meta.addPage("Main Menu \nClick on a button to open a new menu.");
		meta.addPage("Market Items: buy and sell items");
		meta.addPage("Requested Items: request items or fulfill other requests");
		meta.addPage("Free Items: exchange free items on the market");
		book.setItemMeta(meta);
		player.getInventory().addItem(book);
		
		player.sendMessage("A help book has been placed in your inventory.");
		
	}
	
	public static void viewMarketHelp(Player player){
		//make sure player has room
		if (!hasRoom(player.getInventory())){
			player.sendMessage("Clear a spot in your inventory to see the help manual for this menu.");
			return;
		}
		
		//create the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		
		meta.setTitle("GUIM help: view Market Menu");
		meta.setAuthor("");
		
		meta.addPage("View Market Items \nHere, you can click on items to see the sale. Use the two scroll buttons on the bottom to scroll thorugh sales.");
		meta.addPage("Press the 'Sale' button to add items to the market.");
		
		book.setItemMeta(meta);
		player.getInventory().addItem(book);
		
		player.sendMessage("A help book has been placed in your inventory.");
		
	}
	
	public static void viewRequestHelp(Player player){
		//make sure player has room
		if (!hasRoom(player.getInventory())){
			player.sendMessage("Clear a spot in your inventory to see the help manual for this menu.");
			return;
		}
		
		//create the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		
		meta.setTitle("GUIM help: view Requests Menu");
		meta.setAuthor("");
		
		meta.addPage("View Request Items \nHere, you can click on items to see the request. Use the two scroll buttons on the bottom to scroll thorugh requests.");
		meta.addPage("Request items by clicking on a request book.");
		meta.addPage("Request book: one page, with four fields, seperated by commas. \n The book title should be 'request'. Fields are: id,quantity,number_per_unit,price_per_unit");
		meta.addPage("The next page shows an example. The request is for STONE, 100 blocks, in units of 20. Each unit is $40.");
		meta.addPage("1,100,20,40");
		
		book.setItemMeta(meta);
		
		player.getInventory().addItem(book);
		player.sendMessage("A help book has been placed in your inventory.");
	}
	
	public static void buyHelp(Player player){
		//make sure player has room
		if (!hasRoom(player.getInventory())){
			player.sendMessage("Clear a spot in your inventory to see the help manual for this menu.");
			return;
		}
		
		//create the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		
		meta.setTitle("GUIM help: buy Menu");
		meta.addPage("GUIM help: buy a sale\nuse the yellow and blue buttons to choose how many units to buy.");
		meta.addPage("The sticks represent how many you want to buy. The spot on the right is the one's place, the next is the ten's, etc.");
		meta.setAuthor("");
		
		book.setItemMeta(meta);

		player.getInventory().addItem(book);
		player.sendMessage("A help book has been placed in your inventory.");
		
	}
	
	public static void fulfillHelp(Player player){
		//make sure player has room
		if (!hasRoom(player.getInventory())){
			player.sendMessage("Clear a spot in your inventory to see the help manual for this menu.");
			return;
		}
		
		//create the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		
		meta.setTitle("GUIM help: Fulfill Menu");
		meta.addPage("GUIM help: buy a sale\nuse the yellow and blue buttons to choose how many units to fulfill.");
		meta.addPage("The sticks represent how many you want to fulfill. The spot on the right is the one's place, the next is the ten's, etc.");
		meta.setAuthor("");
		
		book.setItemMeta(meta);

		player.getInventory().addItem(book);
		player.sendMessage("A help book has been placed in your inventory.");
		
	}
	
	public static void sellHelp(Player player){
		//make sure player has room
		if (!hasRoom(player.getInventory())){
			player.sendMessage("Clear a spot in your inventory to see the help manual for this menu.");
			return;
		}
		
		//create the book
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		
		meta.setTitle("GUIM help: buy Menu");
		meta.addPage("GUIM help: buy a sale\nThere are three stages to selling items: selecting items/quantity, selecting number per unit, selecting price per unit");
		meta.addPage("The sticks represent how many you want to buy. The spot on the right is the one's place, the next is the ten's, etc.");
		meta.addPage("Stage 1:\nClick on items to add them to the sale. Use the buttons to select the total quantity of each item in the sale.");
		meta.addPage("Stage 2:\nSelect how many items per unit. For example, you might want to sell items in groups of 2,5, or some other number");
		meta.addPage("Stage 3\nFinally, select the price per one unit.");
		meta.setAuthor("");
		
		book.setItemMeta(meta);

		player.getInventory().addItem(book);
		player.sendMessage("A help book has been placed in your inventory.");
		
	}
}
