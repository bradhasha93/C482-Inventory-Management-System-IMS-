package com.ims.content;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ims.main.IMSApplication;

import javafx.scene.control.Alert.AlertType;

public class Inventory {

	private IMSApplication app;

	public Inventory(IMSApplication app) {
		this.app = app;
	}

	// NOTE TO EVALUATOR: Changed return type from void to Product to return product back to controller
	// with the generated ID
	/**
	 * Add a new product
	 * @param product
	 * @return instance of new product for use in controller
	 */
	public Product addProduct(Product product) {
		try {
			Connection conn = app.getDatabase().getConnection();
			// Prepare sql to create new product
			String addProductSql = "INSERT INTO Products(name,price,inStock,min,max) VALUES(?,?,?,?,?)";
			PreparedStatement addProductPstmt = conn.prepareStatement(addProductSql);
			addProductPstmt.setString(1, product.getName());
			addProductPstmt.setDouble(2, product.getPrice());
			addProductPstmt.setInt(3, product.getInStock());
			addProductPstmt.setInt(4, product.getMin());
			addProductPstmt.setInt(5, product.getMax());

			// Execute sql to create new product
			addProductPstmt.executeUpdate();
			product.setProductId(addProductPstmt.getGeneratedKeys().getInt("last_insert_rowid()"));

			// Prepare sql to associate parts to the new product
			String associatedPartIds = "";
			String addAssociatedPartsSql = "INSERT INTO Products_AssociatedParts(productId, partId) VALUES(?,?)";
			PreparedStatement addAssociatedPartsPstmt = conn.prepareStatement(addAssociatedPartsSql);
			addAssociatedPartsPstmt.setInt(1, product.getProductId());

			for (Part part : product.getAssociatedParts()) {
				addAssociatedPartsPstmt.setInt(2, part.getPartId());
				addAssociatedPartsPstmt.executeUpdate();
				associatedPartIds += part.getPartId() + ",";
			}
			associatedPartIds = associatedPartIds.replaceAll(",$", "");

			// Display alert to user to inform of action
			app.alert(AlertType.INFORMATION, "Add Product (ID:" + product.getProductId() + ") Successful",
					"Id: " + product.getProductId() + "\nName: " + product.getName() + "\nPrice: $"
							+ product.getFormattedPrice() + "\nIn Stock: " + product.getInStock() + "\nMin: "
							+ product.getMin() + "\nMax: " + product.getMax() + "\nAssociated Part IDs: "
							+ associatedPartIds);
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Add Product Unsuccessful", e.getMessage());
			e.printStackTrace();
		}
		return product;
	}

	/**
	 * Remove a product and all part association
	 * @param product
	 * @return true if product delete was successful
	 */
	public boolean removeProduct(Product product) {
		try {
			Connection conn = app.getDatabase().getConnection();
			// Prepare and execute sql to delete associated part references from
			// product
			String deleteAssociatedPartsSql = "DELETE FROM Products_AssociatedParts WHERE productId = ?";
			PreparedStatement deleteAssociatedPartsPstmt = conn.prepareStatement(deleteAssociatedPartsSql);
			deleteAssociatedPartsPstmt.setInt(1, product.getProductId());
			deleteAssociatedPartsPstmt.executeUpdate();

			// Prepare and execute sql to delete product
			String deleteProductSql = "DELETE FROM Products WHERE productId = ?";
			PreparedStatement deleteProductPstmt = conn.prepareStatement(deleteProductSql);
			deleteProductPstmt.setInt(1, product.getProductId());
			deleteProductPstmt.executeUpdate();

			// Display alert to user to inform of action
			app.alert(AlertType.INFORMATION, "Delete Product (ID:" + product.getProductId() + ") Successful",
					"Id: " + product.getProductId() + "\nName: " + product.getName());
			return true;
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Delete Product (ID:" + product.getProductId() + ") Unsuccessful",
					e.getMessage());
		}
		return false;
	}

	// UNUSED: Created method to accept product object instead
	public boolean removeProduct(int productId) {
		return false;
	}

	/**
	 * Lookup product by productId
	 * @param productId
	 * @return new instance of product matching the productId
	 */
	public Product lookupProduct(int productId) {
		try {
			return getAllProducts().stream().filter(next -> next.getProductId() == productId).findFirst().get();
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Lookup Product (ID:" + productId + ") Unsuccessful", e.getMessage());
		}
		return null;
	}

	/**
	 * Update a product
	 * @param product
	 */
	public void updateProduct(Product product) {
		try {
			// Lookup current data for product to be updated
			Product oldProduct = lookupProduct(product.getProductId());

			// Prepare and execute sql to update basic product info
			String sql = "UPDATE Products SET name = ? , price = ? , inStock = ? , min = ? , max = ? WHERE productId = ?";
			Connection conn = app.getDatabase().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);

			pstmt.setString(1, product.getName());
			pstmt.setDouble(2, product.getPrice());
			pstmt.setInt(3, product.getInStock());
			pstmt.setInt(4, product.getMin());
			pstmt.setInt(5, product.getMax());
			pstmt.setInt(6, product.getProductId());
			pstmt.executeUpdate();

			// Select all current associated parts for the product and get IDs
			// for string in alert msg
			String oldAssociatedPartIds = "";
			String selectAllAssociatedPartsSql = "SELECT partId FROM Products_AssociatedParts WHERE productId = ?";
			PreparedStatement selectAllAssociatedPartsPstmt = conn.prepareStatement(selectAllAssociatedPartsSql);
			selectAllAssociatedPartsPstmt.setInt(1, product.getProductId());

			ResultSet selectRs = selectAllAssociatedPartsPstmt.executeQuery();
			while (selectRs.next()) {
				oldAssociatedPartIds += selectRs.getInt("partId") + ",";
			}
			oldAssociatedPartIds = oldAssociatedPartIds.replaceAll(",$", "");

			// Prepare and execute sql to delete all associated parts to a
			// product
			String deleteAllAssociatedPartsSql = "DELETE FROM Products_AssociatedParts WHERE productId = ?";
			PreparedStatement deleteAllAssociatedPartsPstmt = conn.prepareStatement(deleteAllAssociatedPartsSql);
			deleteAllAssociatedPartsPstmt.setInt(1, product.getProductId());
			deleteAllAssociatedPartsPstmt.executeUpdate();

			// Prepare and execute sql to update associated parts to the new
			// product
			String associatedPartIds = "";
			String addAssociatedPartsSql = "INSERT INTO Products_AssociatedParts(productId, partId) VALUES(?,?)";
			PreparedStatement addAssociatedPartsPstmt = conn.prepareStatement(addAssociatedPartsSql);
			addAssociatedPartsPstmt.setInt(1, product.getProductId());
			for (Part part : product.getAssociatedParts()) {
				addAssociatedPartsPstmt.setInt(2, part.getPartId());
				addAssociatedPartsPstmt.executeUpdate();
				associatedPartIds += part.getPartId() + ",";
			}
			associatedPartIds = associatedPartIds.replaceAll(",$", "");

			// Alert user with prompt displaying old product info -> new product
			// info
			app.alert(AlertType.INFORMATION, "Update Product (ID:" + product.getProductId() + ") Successful",
					"Name: " + oldProduct.getName() + " -> " + product.getName() + "\nPrice: "
							+ oldProduct.getFormattedPrice() + " -> " + product.getFormattedPrice() + "\nIn Stock: "
							+ oldProduct.getInStock() + " -> " + product.getInStock() + "\nMax: " + oldProduct.getMax()
							+ " -> " + product.getMax() + "\nMin: " + oldProduct.getMin() + " -> " + product.getMin()
							+ "\nAssociated Part IDs: " + oldAssociatedPartIds + " -> " + associatedPartIds);
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Update Product (ID:" + product.getProductId() + ") Unsuccessful",
					e.getMessage());
		}
	}

	/**
	 * Get a list of all products
	 * @return
	 */
	public List<Product> getAllProducts() {
		try {
			List<Product> products = new ArrayList<>();
			Connection conn = app.getDatabase().getConnection();
			// Prepare sql
			String selectAllProductsSql = "SELECT * FROM Products;";
			Statement stmt = conn.createStatement();

			// Execute sql and process result set
			ResultSet rs = stmt.executeQuery(selectAllProductsSql);

			while (rs.next()) {
				final int productId = rs.getInt("productId");
				final String name = rs.getString("name");
				final double price = rs.getDouble("price");
				final int inStock = rs.getInt("inStock");
				final int min = rs.getInt("min");
				final int max = rs.getInt("max");

				Product product = new Product();
				product.setProductId(productId);
				product.setName(name);
				product.setPrice(price);
				product.setInStock(inStock);
				product.setMin(min);
				product.setMax(max);

				// Find all associated parts to a product to setup
				// associatedParts on each product
				String selectAllAssociatedPartsSql = "SELECT * FROM Products_AssociatedPartsView WHERE productId = "
						+ productId + ";";
				Statement selectAllAssociatedPartsStmt = conn.createStatement();

				ResultSet selectAllAssociatedPartsRs = selectAllAssociatedPartsStmt
						.executeQuery(selectAllAssociatedPartsSql);
				while (selectAllAssociatedPartsRs.next()) {
					product.addAssociatedPart(lookupPart(selectAllAssociatedPartsRs.getInt("partId")));
				}

				products.add(product);
			}
			return products;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	// UNUSED: Created method to accept Product instead of productId to provide a way to
	// update information of a product
	public void updateProduct(int productId) {
	}

	// NOTE TO EVALUATOR: Changed return type to Part to return part back to controller to
	// set generated ID in GUI
	/**
	 * Add a part
	 * @param part
	 * @return instance of newly created part
	 */
	public Part addPart(Part part) {
		try {
			String specialInfo;

			// Prepare sql
			String sql = "INSERT INTO Parts(name,price,inStock,min,max,machineId,companyName) VALUES(?,?,?,?,?,?,?)";
			Connection conn = app.getDatabase().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, part.getName());
			pstmt.setDouble(2, part.getPrice());
			pstmt.setInt(3, part.getInStock());
			pstmt.setInt(4, part.getMin());
			pstmt.setInt(5, part.getMax());

			// Determine part type
			if (part instanceof Inhouse) {
				final int machineId = ((Inhouse) part).getMachineId();
				pstmt.setInt(6, machineId);
				pstmt.setString(7, "EMPTY");
				specialInfo = "\nMachine ID: " + machineId + "\n" + "Company Name: EMPTY";
			} else {
				final String companyName = ((Outsourced) part).getCompanyName();
				pstmt.setInt(6, 0);
				pstmt.setString(7, companyName);
				specialInfo = "\nMachine ID: 0\nCompany Name: " + companyName;
			}

			// Execute sql
			pstmt.executeUpdate();
			part.setPartId(pstmt.getGeneratedKeys().getInt("last_insert_rowid()"));

			// Display alert to user to inform of action
			app.alert(AlertType.INFORMATION, "Add Part Successful",
					"Id: " + part.getPartId() + "\nName: " + part.getName() + "\nPrice: $" + part.getFormattedPrice()
							+ "\nIn Stock: " + part.getInStock() + "\nMin: " + part.getMin() + "\nMax: " + part.getMax()
							+ specialInfo);
			return part;
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Add Part Unsuccessful", e.getMessage());
		}
		return null;
	}

	/**
	 * Delete a part
	 * @param part
	 * @return true if successful deletion of part
	 */
	public boolean deletePart(Part part) {
		try {
			// Prepare sql
			String sql = "DELETE FROM Parts WHERE partId = ?";
			Connection conn = app.getDatabase().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, part.getPartId());

			// Execute sql
			pstmt.executeUpdate();

			// Display alert to user to inform of action
			app.alert(AlertType.INFORMATION, "Delete Part (ID:" + part.getPartId() + ") Successful",
					"Id: " + part.getPartId() + "\nName: " + part.getName());
			return true;
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Delete Part (ID:" + part.getPartId() + ") Unsuccessful", e.getMessage());
		}
		return false;
	}

	/**
	 * Lookup a part by partId
	 * @param partId
	 * @return instance of part matching the partId
	 */
	public Part lookupPart(int partId) {
		try {
			return getAllParts().stream().filter(next -> next.getPartId() == partId).findFirst().get();
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Lookup Part (ID:" + partId + ") Unsuccessful", e.getMessage());
		}
		return null;
	}

	/**
	 * Update a part
	 * @param part
	 */
	public void updatePart(Part part) {
		try {
			// Variables for alert msg
			String oldPartMachineId;
			String oldPartCompanyName;
			String updatedPartSpecialTxt;

			// Lookup current data for part to be updated
			Part oldPart = lookupPart(part.getPartId());

			// Prepare sql
			String sql = "UPDATE Parts SET name = ? , price = ? , inStock = ? , min = ? , max = ? , machineId = ? , companyName = ? WHERE partId = ?";
			Connection conn = app.getDatabase().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);

			// Prepare alert text based off of old part type
			if (oldPart instanceof Inhouse) {
				oldPartMachineId = "" + ((Inhouse) oldPart).getMachineId();
				oldPartCompanyName = "EMPTY";
			} else {
				oldPartMachineId = "0";
				oldPartCompanyName = ((Outsourced) oldPart).getCompanyName();
			}

			// Determine updated part type and determine alert text
			if (part instanceof Inhouse) {
				final int machineId = ((Inhouse) part).getMachineId();
				pstmt.setInt(6, machineId);
				pstmt.setString(7, "EMPTY");
				updatedPartSpecialTxt = "\nMachine ID: " + oldPartMachineId + " -> " + machineId + "\n"
						+ "Company Name: " + oldPartCompanyName + " -> EMPTY";
			} else {
				final String companyName = ((Outsourced) part).getCompanyName();
				pstmt.setInt(6, 0);
				pstmt.setString(7, companyName);
				updatedPartSpecialTxt = "\nMachine ID: " + oldPartMachineId + " -> 0\n" + "Company Name: "
						+ oldPartCompanyName + " -> " + companyName;
			}

			pstmt.setString(1, part.getName());
			pstmt.setDouble(2, part.getPrice());
			pstmt.setInt(3, part.getInStock());
			pstmt.setInt(4, part.getMin());
			pstmt.setInt(5, part.getMax());
			pstmt.setInt(8, part.getPartId());

			// Execute sql
			pstmt.executeUpdate();

			app.alert(AlertType.INFORMATION, "Update Part (ID:" + part.getPartId() + ") Successful",
					"Name: " + oldPart.getName() + " -> " + part.getName() + "\nPrice: " + oldPart.getFormattedPrice()
							+ " -> " + part.getFormattedPrice() + "\nIn Stock: " + oldPart.getInStock() + " -> "
							+ part.getInStock() + "\nMax: " + oldPart.getMax() + " -> " + part.getMax() + "\nMin: "
							+ oldPart.getMin() + " -> " + part.getMin() + updatedPartSpecialTxt);
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Update Part (ID:" + part.getPartId() + ") Unsuccessful", e.getMessage());
		}
	}

	/**
	 * Get a list of all parts
	 * @return
	 */
	public List<Part> getAllParts() {
		try {
			List<Part> parts = new ArrayList<>();
			// Prepare sql
			String sql = "SELECT * FROM Parts;";
			Connection conn = app.getDatabase().getConnection();
			Statement stmt = conn.createStatement();

			// Execute sql and process result set
			ResultSet rs = stmt.executeQuery(sql);

			while (rs.next()) {
				final int partId = rs.getInt("partId");
				final String name = rs.getString("name");
				final double price = rs.getDouble("price");
				final int inStock = rs.getInt("inStock");
				final int min = rs.getInt("min");
				final int max = rs.getInt("max");
				final int machineId = rs.getInt("machineId");
				final String companyName = rs.getString("companyName");

				Part part;
				if (machineId == 0) {
					part = new Inhouse();
					((Inhouse) part).setMachineId(machineId);
				} else {
					part = new Outsourced();
					((Outsourced) part).setCompanyName(companyName);
				}
				part.setPartId(partId);
				part.setName(name);
				part.setPrice(price);
				part.setInStock(inStock);
				part.setMin(min);
				part.setMax(max);

				parts.add(part);
			}
			return parts;
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * Get a list of all products that are associated to a part
	 * @param part
	 * @return list of products associated to specified part
	 */
	public List<Product> getAllAssociatedProducts(Part part) {
		try {
			List<Product> products = new ArrayList<>();

			// Prepare sql
			String sql = "SELECT * FROM Products_AssociatedParts WHERE partId = ?";
			Connection conn = app.getDatabase().getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, part.getPartId());

			// Execute sql and process result set
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				products.add(lookupProduct(rs.getInt("productId")));
			}
			
			return products;
		} catch (Exception e) {
		}
		return null;
	}

	// UNUSED: Created method to accept Part instead of partId to provide a way to
	// update information of a part
	public void updatePart(int partId) {
	}

}
