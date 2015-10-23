package com.m0pt0pmatt.GUIM;

import java.util.LinkedList;
import java.util.UUID;

import org.bukkit.inventory.ItemStack;

import com.m0pt0pmatt.bettereconomy.accounts.UUIDFetcher;

public class ServerMarketSale extends MarketSale {
	
	/**
	 * Default constructor
	 * @param seller
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
	
	@Override
	public UUID getSeller() {
		UUID result = null;
		try {
			result = UUIDFetcher.getUUIDOf("__Server_");
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		return result;
	}
}
