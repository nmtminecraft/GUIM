package com.m0pt0pmatt.GUIM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import com.m0pt0pmatt.GUIM.Player.PlayerInfo;
import net.milkbowl.vault.item.Items;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.m0pt0pmatt.bettereconomy.accounts.UUIDFetcher;

/**
 * Represents a sale on a market. Could be a normal sale, free item(s), or requested item(s)
 * A MarketSale is a multiple of units, where a unit is a collection of items. This is confusing, so here's an example
 * A unit is 5 coal and 5 wooden blocks. The sale has 20 units at $40 per unit
 * @author Matthew Broomfield, James Pelster
 *
 */
public class MarketSale {

	/**
	 * The items that are part of the MarketSale
	 */
	protected LinkedList<ItemStack> items;
	
	/**
	 * The seller/requester of the item(s)
	 */
	private UUID seller;
	
	/**
	 * the price of one bulk unit
	 */
	protected int unitPrice;
	
	/**
	 * the number of items per bulk unit
	 */
	protected int unitSize;
	
	/**
	 * the total number of items which are in the entire sale
	 */
	protected int quantity;
	
	/**
	 * the number of units which have been fulfilled (request only)
	 */
	private int fulfilled;
	
	/**
	 * the number of units that have been purchased
	 */
	private int pickedUp;
	
	/**
	 * Default constructor
	 * @param seller
	 */
	public MarketSale(UUID seller) {
		this.seller = seller;
		this.unitPrice = 0;
		this.unitSize = 1;
		this.quantity = 1;
		this.fulfilled = 0;
        this.pickedUp = 0;
		items = new LinkedList<ItemStack>();
	}
	
	public MarketSale(UUID seller, int price, int bulk, int quantity) {
		this.seller = seller;
		this.unitPrice = price;
		this.unitSize = bulk;
		this.quantity = quantity;
		this.fulfilled = 0;
		this.pickedUp = 0;
		items = new LinkedList<ItemStack>();
	}
	
	public MarketSale(UUID seller, int price, int bulk, int quantity, LinkedList<ItemStack> items, int fulfilled, int pickedUp) {
		this.seller = seller;
		this.unitPrice = price;
		this.unitSize = bulk;
		this.quantity = quantity;
		this.items = items;
		this.fulfilled = fulfilled;
		this.pickedUp = pickedUp;
	}
	
	/**
	 * @return the seller of the ItemStack
	 */
	public UUID getSeller() {
		return seller;
	}
	
	/**
	 * @return the price of the ItemStack
	 */
	public int getUnitPrice(){
		return unitPrice;
	}
	
	/**
	 * @return the number of items per bulk unit
	 */
	public int getUnitSize(){
		return unitSize;
	}
	
	public int getTotalQuantity(){
		return quantity;
	}
	
	public int getAvailiableUnits() {
        return (quantity / unitSize) - fulfilled - pickedUp;
	}
	
	public int getTotalUnitQuantity(){
		return quantity / unitSize;
	}
	
	public int getFulfilled(){
		return fulfilled;
	}
	
	public int getPickedUp(){
		return pickedUp;
	}
	
	public LinkedList<ItemStack> getItems(){
		return items;
	}
	
	public void addItem(ItemStack item){
		items.add(item);
	}
	
	public void removeItem(ItemStack item){
		items.remove(item);
	}
	
	/**
	 * Sets the price of the ItemStack
	 * @param price the price of the ItemStack to be set
	 */
	public void setUnitPrice(int price){
		this.unitPrice = price;
	}
	
	/**
	 * Sets the seller of the ItemStack
	 * @param seller the seller of the ItemStack to be set
	 */
	public void setSeller(UUID seller){
		this.seller = seller;
	}

	/**
	 * Sets the number of items per bulk unit
	 * @param unitSize the number of items to be in each bulk unit
	 */
	public void setUnitSize(int unitSize){
		this.unitSize = unitSize;
	}
	
	public void setTotalQuantity(int quantity){
		this.quantity = quantity;
	}
	
	public void setFulfilled(int fulfilled){
		this.fulfilled = fulfilled;
	}
	
	public void setPickedUp(int pickedUp){
		this.pickedUp = pickedUp;
	}

	public String getSaleInfo(PlayerInfo buyerInfo, boolean playerIsSeller) {
        String saleInfo = "";

        if (seller != null) {
            if (playerIsSeller) {
                saleInfo += "Buyer: " + buyerInfo.name + "\n";
            } else {
                saleInfo += "Seller: " + Bukkit.getOfflinePlayer(seller).getName() + "\n";
            }
        }

        saleInfo += "Units purchased: " + buyerInfo.unitQuantity + "\n";
        saleInfo += "Price per Unit: $" + unitPrice + "\n";
        saleInfo += "Total Price: $" + unitPrice * buyerInfo.unitQuantity + "\n";
        saleInfo += "\n";
        saleInfo += "Total Items Purchased:\n";
        for (ItemStack stack : items)
        {
            saleInfo += unitSize * buyerInfo.unitQuantity + " " + Items.itemByStack(stack).getName();
            if (items.indexOf(stack) < items.size() - 1)
                saleInfo += "\n";
        }
        return saleInfo;
	}
	
	/**
	 * Changes a MarketSale into a hash so it can be stored in a file
	 * @return
	 */
	public Map<String,Object> serialize() {
		Map<String,Object> m = new HashMap<String, Object>();
		
		//first, add the easy fields
        if (seller != null)
            m.put("seller", seller.toString());
		m.put("unitPrice", unitPrice);
		m.put("unitSize", unitSize);
		m.put("quantity", quantity);
		m.put("fulfilled", fulfilled);
		m.put("pickedUp", pickedUp);
		
		//create a map that maps to each item
		Map<String, Object> itemsMap = new HashMap<String, Object>();
		
		//add item mappings to that map
		for (int i = 0; i < items.size(); i++){
			itemsMap.put(String.valueOf(i), items.get(i).serialize());
		}
		
		//add the itemsMap to the final map.
		m.put("items", itemsMap);
		
		return m;
	}
	
	/**
	 * Creates a MarketSale from the given hash
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static MarketSale deserialize(Map<String, Object> args) {
		UUID seller = null;
		int unitPrice = 0;
		int unitSize = 1;
		int quantity = 1;
		int fulfilled = 0;
		int pickedUp = 0;
		
		//first, get the easy fields
		if (args.containsKey("seller")) {
			try {
			    // if the value stored is a UUID
			    seller = UUID.fromString((String) args.get("seller"));
			} catch (IllegalArgumentException e) {
				try {
				    // if, for whatever reason, the value is a username
					seller = UUIDFetcher.getUUIDOf((String) args.get("seller"));
				} catch (Exception e2) {
					Bukkit.getLogger().warning("Unable to determine player:\n" + ChatColor.RED + args.get("seller") + ChatColor.RESET);
				}
			}
		}


        if (args.containsKey("unitPrice")){
            unitPrice = (Integer) args.get("unitPrice");
        } else if (args.containsKey("price")){
			unitPrice = (Integer) args.get("price");
		}

        if (args.containsKey("unitSize")){
            unitSize = (Integer) args.get("unitSize");
        } else if (args.containsKey("bulk")){
            unitSize = (Integer) args.get("bulk");
        }
		
		if (args.containsKey("quantity")){
			quantity = (Integer) args.get("quantity");
		}
		
		if (args.containsKey("fulfilled")){
			fulfilled = (Integer) args.get("fulfilled");
		}
		
		if (args.containsKey("pickedUp")){
			pickedUp = (Integer) args.get("pickedUp");
		}
		
		//now, get the itemsMap
		LinkedList<ItemStack> items = new LinkedList<ItemStack>();
		if (args.containsKey("items")){
			items = (LinkedList<ItemStack>) args.get("items");
		}
		
		//create the marketsale
		MarketSale m = new MarketSale(seller, unitPrice, unitSize, quantity, items, fulfilled, pickedUp);

		return m;
	}
	
}
