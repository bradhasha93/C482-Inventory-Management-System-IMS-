package com.ims.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ims.content.Part;
import com.ims.content.Product;
import com.ims.main.IMSApplication;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class IMSMainController extends Controller implements Initializable {

	private IMSApplication app;

	private ObservableList<Part> parts;
	private ObservableList<Product> products;

	// Buttons
	@FXML
	private Button searchPartsBtn, addPartsBtn, modifyPartsBtn, deletePartsBtn, searchProductsBtn, addProductsBtn,
			modifyProductsBtn, deleteProductsBtn, exitBtn;

	// Textfields
	@FXML
	private TextField searchPartsTxt, searchProductsTxt;

	// Parts Table
	@FXML
	private TableView<Part> partsTbl;

	@FXML
	private TableColumn<Part, String> partIdColumn, partNameColumn, partInStockColumn, partPriceColumn;

	// Products Table
	@FXML
	private TableView<Product> productsTbl;

	@FXML
	private TableColumn<Product, String> productIdColumn, productNameColumn, productInStockColumn, productPriceColumn;

	public IMSMainController(IMSApplication app) {
		super("IMSMain.fxml");
		this.app = app;
	}

	@Override
	public void handleClick(ActionEvent event) {
		switch (((Button) event.getSource()).getId()) {
		case "exitBtn":
			app.exit();
			break;
		case "addPartsBtn":
			app.getPartController().loadScene(event);
			app.getPartController().setMainLbl(event, null);
			break;
		case "modifyPartsBtn":
			Part modifyPart = partsTbl.getSelectionModel().getSelectedItem();
			if (modifyPart == null) {
				app.alert(AlertType.ERROR, "No Part Selected",
						"Please select a part from the table to perform this action.");
				return;
			}
			app.getPartController().loadScene(event);
			app.getPartController().setMainLbl(event, modifyPart);
			break;
		case "deletePartsBtn":
			Part deletePart = partsTbl.getSelectionModel().getSelectedItem();
			if (deletePart == null) {
				app.alert(AlertType.ERROR, "No Part Selected",
						"Please select a part from the table to perform this action.");
				return;
			}
			deletePart(deletePart);
			break;
		case "addProductsBtn":
			app.getProductController().loadScene(event);
			app.getProductController().setMainLbl(event, null);
			break;
		case "modifyProductsBtn":
			Product modifyProduct = productsTbl.getSelectionModel().getSelectedItem();
			if (modifyProduct == null) {
				app.alert(AlertType.ERROR, "No Product Selected",
						"Please select a product from the table to perform this action.");
				return;
			}
			app.getProductController().loadScene(event);
			app.getProductController().setMainLbl(event, modifyProduct);
			break;
		case "deleteProductsBtn":
			Product deleteProduct = productsTbl.getSelectionModel().getSelectedItem();
			if (deleteProduct == null) {
				app.alert(AlertType.ERROR, "No Product Selected",
						"Please select a product from the table to perform this action.");
				return;
			}
			deleteProduct(deleteProduct);
			break;
		case "searchPartsBtn":
			loadPartsTbl();
			break;
		case "searchProductsBtn":
			loadProductsTbl();
			break;
		}
	}

	/**
	 * Delete a part
	 * @param part
	 */
	private void deletePart(Part part) {
		String associatedProductIds = "";
		List<Product> products = app.getInventory().getAllAssociatedProducts(part);

		if (products.size() > 0) {
			for (Product product : products) {
				associatedProductIds += product.getProductId() + ",";
			}
			associatedProductIds = associatedProductIds.replaceAll(",$", "");

			app.alert(AlertType.ERROR, "Unable to Delete Part (ID:" + part.getPartId() + ")",
					"This part is associated to the following product IDs: " + associatedProductIds
							+ "\n\nThe part must be disassociated from the products before it can be deleted.");
		} else if (app.alert(AlertType.CONFIRMATION, "Prompt",
				"Are you sure you want to delete Part (ID:" + part.getPartId() + ")?")) {
			app.getInventory().deletePart(part);
			loadAllTables();
		}
	}

	/**
	 * Delete a product
	 * @param product
	 */
	private void deleteProduct(Product product) {
		if (app.alert(AlertType.CONFIRMATION, "Prompt",
				"Are you sure you want to delete Product (ID:" + product.getProductId()
						+ ")?\n\nNote: This will also remove all associated part references for this product.")) {
			app.getInventory().removeProduct(product);
			loadAllTables();
		}
	}

	/**
	 * Load parts table and apply search filter if applicable
	 */
	private void loadPartsTbl() {
		parts = FXCollections.observableArrayList(app.getInventory().getAllParts().stream().filter(next -> {
			final String data = next.getPartId() + next.getName() + next.getInStock() + next.getFormattedPrice();
			return data.toLowerCase().contains(searchPartsTxt.getText().toLowerCase());
		}).collect(Collectors.toList()));
		partsTbl.setItems(parts);
	}

	/**
	 * Load products table and apply search filter if applicable
	 */
	private void loadProductsTbl() {
		products = FXCollections.observableArrayList(app.getInventory().getAllProducts().stream().filter(next -> {
			final String data = next.getProductId() + next.getName() + next.getInStock() + next.getFormattedPrice();
			return data.toLowerCase().contains(searchProductsTxt.getText().toLowerCase());
		}).collect(Collectors.toList()));
		productsTbl.setItems(products);
	}

	/**
	 * Load both the parts and products table
	 */
	private void loadAllTables() {
		loadPartsTbl();
		loadProductsTbl();
	}

	@Override
	public void loadScene(Stage stage) {
		super.loadScene(stage);
		loadAllTables();
	}

	@Override
	public void loadScene(ActionEvent event) {
		super.loadScene(event);
		loadAllTables();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Bind parts table
		partIdColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("partId"));
		partNameColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("name"));
		partInStockColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("inStock"));
		partPriceColumn.setCellValueFactory(cellData -> Bindings.format("%.2f", cellData.getValue().getPrice()));

		// Bind products table
		productIdColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("productId"));
		productNameColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("name"));
		productInStockColumn.setCellValueFactory(new PropertyValueFactory<Product, String>("inStock"));
		productPriceColumn.setCellValueFactory(cellData -> Bindings.format("%.2f", cellData.getValue().getPrice()));
		
		// Bind change listeners for auto-searching
		searchPartsTxt.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				loadPartsTbl();
			}
		});
		
		searchProductsTxt.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				loadProductsTbl();
			}
		});
	}
}
