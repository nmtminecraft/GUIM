package com.m0pt0pmatt.GUIM;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

/**
 * Represents a sale on a market. Could be a normal sale, free item(s), or requested item(s)
 * A MarketSale is a multiple of units, where a unit is a collection of items. This is confusing, so here's an example
 * A unit is 5 coal and 5 wooden blocks. The sale has 20 units at $40 per unit
 * @author Matthew
 *
 */
public class MarketSale {

	/**
	 * The items that are the parts of the MarketSale
	 */
	private LinkedList<ItemStack> items;
	
	/**
	 * The seller/requester of the item(s)
	 */
	private String seller;
	
	/**
	 * the price of one sale unit
	 */
	private int unitPrice;
	
	/**
	 * the number of units
	 */
	private int numPerUnits;
	
	/**
	 * the total number of items which are in the entire sale
	 */
	private int quantity;
	
	/**
	 * the number of units which have been fulfilled (request only)
	 */
	private int fulfilled;
	
	/**
	 * the number of units that have been picked up by the seller 
	 */
	private int pickedUp;
	
	
	public MarketSale(){
		this.seller = "";
		this.unitPrice = 0;
		this.numPerUnits = 1;
		this.quantity = 1;
		this.fulfilled = 0;
		items = new LinkedList<ItemStack>();
	}
	
	/**
	 * Default constructor
	 * @param seller
	 */
	public MarketSale(String seller){
		this.seller = seller;
		this.unitPrice = 0;
		this.numPerUnits = 1;
		this.quantity = 1;
		this.fulfilled = 0;
		items = new LinkedList<ItemStack>();
	}
	
	public MarketSale(String seller, int price, int bulk, int quantity) {
		this.seller = seller;
		this.unitPrice = price;
		this.numPerUnits = bulk;
		this.quantity = quantity;
		this.fulfilled = 0;
		this.pickedUp = 0;
		items = new LinkedList<ItemStack>();
	}
	
	public MarketSale(String seller, int price, int bulk, int quantity, LinkedList<ItemStack> items, int fulfilled, int pickedUp) {
		this.seller = seller;
		this.unitPrice = price;
		this.numPerUnits = bulk;
		this.quantity = quantity;
		this.items = items;
		this.fulfilled = fulfilled;
		this.pickedUp = pickedUp;
	}
	
	/**
	 * Returns the seller of the ItemStack
	 * @return the seller of the ItemStack
	 */
	public String getSeller(){
		return seller;
	}
	
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
	public void setSeller(String seller){
		this.seller = seller;
	}

	/**
	 * Sets the ItemStack to be or not be for bulk sale
	 * @param bulk
	 */
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
	
	@Override
	public String toString(){
		return super.toString() + " price " + this.unitPrice + " seller " + this.seller + " isBulk " + numPerUnits;		
	}
	
	/**
	 * Changes a MarketSale into a hash so it can be stored in a file
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
	 * Creates a MarketSale from the given hash
	 * @param args
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static MarketSale deserialize(Map<String, Object> args){
		String seller = null;
		int price = 0;
		int bulk = 1;
		int quantity = 1;
		int fulfilled = 0;
		int pickedUp = 0;
		
		//first, get the easy fields
		if (args.containsKey("seller")){
			seller = (String) args.get("seller");
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
			int i = 0;
			items = (LinkedList<ItemStack>) args.get("items");
		}
		
		//create the marketsale
		MarketSale m = new MarketSale(seller, price, bulk, quantity, items, fulfilled, pickedUp);

		return m;
		
	}
}
