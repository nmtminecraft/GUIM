package com.m0pt0pmatt.GUIM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import com.m0pt0pmatt.bettereconomy.accounts.UUIDFetcher;

public class ServerMarketSale {

    /**
     * The items that are the parts of the MarketSale
     */
    protected LinkedList<ItemStack> items;

    /**
     * The seller/requester of the item(s)
     */
    private String seller;

    /**
     * the price of one sale unit
     */
    protected int unitPrice;

    /**
     * the number of units
     */
    protected int numPerUnits;

    /**
     * the total number of items which are in the entire sale
     */
    protected int quantity;

    /**
     * the number of units which have been fulfilled (request only)
     */
    private int fulfilled;

    /**
     * the number of units that have been picked up by the seller
     */
    private int pickedUp;

    /**
     * Returns the price of the ItemStack
     * @return the price of the ItemStack
     */
    public int getUnitPrice(){
        return unitPrice;
    }

    /**
     * Returns whether or not the ItemStack is for bulk sale
     * @return whether or not the ItemStack is for bulk sale
     */
    public int getNumPerUnits(){
        return numPerUnits;
    }

    public int getTotalQuantity(){
        return quantity;
    }

    public int getAvailiableUnits(){
        return (quantity / numPerUnits) - fulfilled - pickedUp;
    }

    public int getUnitQuantity(){
        return quantity / numPerUnits;
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
    public void setPrice(int price){
        this.unitPrice = price;
    }

    /**
     * Sets the seller of the ItemStack
     * @param seller the seller of the ItemStack to be set
     */
    public void setSeller(UUID seller){
        this.seller = seller.toString();
    }

    public void setNumPerUnits(int numPerUnits){
        this.numPerUnits = numPerUnits;
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

	/**
	 * Default constructor
	 */
	public ServerMarketSale() {
		this.unitPrice = 0;
		this.numPerUnits = 1;
		this.quantity = -1;
		items = new LinkedList<ItemStack>();
	}
	
	public ServerMarketSale(int price, int bulk) {
		this.unitPrice = price;
		this.numPerUnits = bulk;
		this.quantity = -1;
		items = new LinkedList<ItemStack>();
	}
	
	public ServerMarketSale(int price, int bulk, int quantity) {
		this.unitPrice = price;
		this.numPerUnits = bulk;
		this.quantity = quantity;
		items = new LinkedList<ItemStack>();
	}
	
	public ServerMarketSale(int price, int bulk, int quantity, LinkedList<ItemStack> items, int fulfilled, int pickedUp) {
		this.unitPrice = price;
		this.numPerUnits = bulk;
		this.quantity = quantity;
		this.items = items;
	}

	public UUID getSeller() {
		UUID result = null;
		try {
			result = UUIDFetcher.getUUIDOf("__Server_");
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		return result;
	}

	@Override
	public String toString(){
		String itemDescription = "\n" + ChatColor.GREEN;
		if (items != null && items.size() == 1) {
			itemDescription += items.get(0).getType().toString();
		} else {
			itemDescription += "Item Package";
		}

		itemDescription += ChatColor.RESET;
		OfflinePlayer p;
		try {
			p = Bukkit.getOfflinePlayer(UUID.fromString(seller));
		} catch (IllegalArgumentException E) {
			p = null;
		}
		if (p != null) {
			this.seller = p.getName();
		}

		return itemDescription + "\nPrice: " + ChatColor.DARK_RED + this.unitPrice
				+ ChatColor.RESET + "\nSeller: " + this.seller;
//		return "{"
//				+ "seller " + Bukkit.getOfflinePlayer(UUID.fromString(this.seller)).getName() + ", "
//				+ "items: " + items.toString() + ", "
//				+ "price " + this.unitPrice + ", "
//				+ "isBulk " + numPerUnits
//				+ "}";
	}

	/**
	 * Changes a ServerMarketSale into a hash so it can be stored in a file
	 * @return
	 */
	public Map<String,Object> serialize(){
		Map<String,Object> m = new HashMap<String,Object>();

		//first, add the easy fields
		m.put("price", unitPrice);
		m.put("seller", seller);
		m.put("bulk", numPerUnits);
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
	 * Creates a ServerMarketSale from the given hash
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static ServerMarketSale deserialize(Map<String, Object> args){
		UUID seller = null;
		int price = 0;
		int bulk = 1;
		int quantity = 1;
		int fulfilled = 0;
		int pickedUp = 0;

		//first, get the easy fields
		if (args.containsKey("seller")){
			try {
				seller = UUID.fromString((String) args.get("seller"));
			} catch (IllegalArgumentException e) {
				try {
					seller = UUIDFetcher.getUUIDOf((String) args.get("seller"));
				} catch (Exception e2) {
					Bukkit.getLogger().warning("Unable to determine player:\n" + ChatColor.RED + (String) args.get("seller") + ChatColor.RESET);
				}
			}
		}

		if (args.containsKey("price")){
			price = (Integer) args.get("price");
		}

		if (args.containsKey("bulk")){
			bulk = (Integer) args.get("bulk");
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
		ServerMarketSale m = new ServerMarketSale(price, bulk, quantity, items, fulfilled, pickedUp);

		return m;

	}
}
