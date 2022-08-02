package com.ims.content;

import java.text.DecimalFormat;

public abstract class Part {
	
	private int partId;
	private String name;
	private double price;
	private int inStock;
	private int min;
	private int max;
	private DecimalFormat df = new DecimalFormat("#0.00");

	/**
	 * Get partId of part
	 * @return partId
	 */
	public int getPartId() {
		return partId;
	}

	/**
	 * Set partId of part
	 * @param partId
	 */
	public void setPartId(int partId) {
		this.partId = partId;
	}

	/**
	 * Get name of part
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of part
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get price of part
	 * @return price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Set price of part
	 * @param price
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Get inventory level of part
	 * @return inStock
	 */
	public int getInStock() {
		return inStock;
	}

	/**
	 * Set inventory level of part
	 * @param inStock
	 */
	public void setInStock(int inStock) {
		this.inStock = inStock;
	}

	/**
	 * Get min inventory level of part
	 * @return min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Set min inventory level of part
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * Get max inventory level of part
	 * @return
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Set max inventory level of part
	 * @param max
	 */
	public void setMax(int max) {
		this.max = max;
	}
	
	/**
	 * Get formatted price of part
	 * @return formatted price
	 */
	public String getFormattedPrice() {
		return df.format(getPrice());
	}
}
