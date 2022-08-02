package com.ims.content;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Product {

	private ArrayList<Part> associatedParts = new ArrayList<>();
	private int productId;
	private String name;
	private double price;
	private int inStock;
	private int min;
	private int max;
	private DecimalFormat df = new DecimalFormat("#0.00");

	public ArrayList<Part> getAssociatedParts() {
		return this.associatedParts;
	}

	/**
	 * Add associated part to product
	 * @param part
	 */
	public void addAssociatedPart(Part part) {
		associatedParts.add(part);
	}

	/**
	 * Remove associated part from product
	 * @param part
	 * @return true if part was removed
	 */
	public boolean removeAssociatedPart(Part part) {
		return associatedParts.removeIf(p -> p.getPartId() == part.getPartId());
	}

	// UNUSED: Created method to accept Part instead
	public boolean removeAssociatedPart(int partId) {
		return false;
	}

	// UNUSED
	public Part lookupAssociatedPart(int partId) {
		return null;
	}

	/**
	 * Get productId of product
	 * @return productId
	 */
	public int getProductId() {
		return productId;
	}

	/**
	 * Set productId of product
	 * @param productId
	 */
	public void setProductId(int productId) {
		this.productId = productId;
	}

	/**
	 * Get name of product
	 * @return name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of product
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get price of product
	 * @return price
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Set price of product
	 * @param price
	 */
	public void setPrice(double price) {
		this.price = price;
	}

	/**
	 * Get inventory level of product
	 * @return inStock
	 */
	public int getInStock() {
		return inStock;
	}

	/**
	 * Set inventory level of product
	 * @param inStock
	 */
	public void setInStock(int inStock) {
		this.inStock = inStock;
	}

	/**
	 * Get min inventory level of product
	 * @return min
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Set min inventory level of product
	 * @param min
	 */
	public void setMin(int min) {
		this.min = min;
	}

	/**
	 * Get max inventory level of product
	 * @return max
	 */
	public int getMax() {
		return max;
	}

	/**
	 * Set max inventory level of product
	 * @param max
	 */
	public void setMax(int max) {
		this.max = max;
	}

	/**
	 * Check to see if product can be created or updated. 
	 * @return true if price of prodouct is greater than or equal to the total price of the associated parts.
	 */
	public boolean isValidProduct() {
		return getPrice() >= getTotalPriceOfAssociatedParts();
	}

	/**
	 * Get total price of all associated parts
	 * @return total price
	 */
	public double getTotalPriceOfAssociatedParts() {
		double price = 0;
		for (Part part : associatedParts)
			price += part.getPrice();
		return price;
	}

	/**
	 * Get formatted price of product
	 * @return formatted price
	 */
	public String getFormattedPrice() {
		return df.format(getPrice());
	}
}
