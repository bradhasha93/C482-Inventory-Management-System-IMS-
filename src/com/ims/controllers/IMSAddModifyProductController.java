package com.ims.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.ims.content.Part;
import com.ims.content.Product;
import com.ims.content.ValidatedTextField;
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
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class IMSAddModifyProductController extends Controller implements Initializable {

	private IMSApplication app;

	private ObservableList<Part> available, associated;
	private Product product;
	// Labels
	@FXML
	private Label mainLbl, specialLbl;

	// Textfields
	@FXML
	private TextField idTxt, addSearchTxt, deleteSearchTxt;

	@FXML
	private ValidatedTextField nameTxt, maxTxt, minTxt, invTxt, priceCostTxt;

	// Buttons
	@FXML
	private Button cancelBtn, saveBtn, addSearchBtn, addBtn, deleteSearchBtn, deleteBtn;

	// Add Parts Table
	@FXML
	private TableView<Part> addPartsTbl;

	@FXML
	private TableColumn<Part, String> addPartIdColumn, addPartNameColumn, addInStockColumn, addPriceColumn;

	// Delete Parts Table
	@FXML
	private TableView<Part> deletePartsTbl;

	@FXML
	private TableColumn<Part, String> deletePartIdColumn, deletePartNameColumn, deleteInStockColumn, deletePriceColumn;

	public IMSAddModifyProductController(IMSApplication app) {
		super("IMSAddModifyProduct.fxml");
		this.app = app;
	}

	@Override
	public void handleClick(ActionEvent event) {
		switch (((Control) event.getSource()).getId()) {
		case "cancelBtn":
			if (app.alert(AlertType.CONFIRMATION, "Prompt", "Are you sure you want to return to the main screen?")) {
				app.getMainController().loadScene(event);
			}
			break;
		case "saveBtn":
			if (saveProduct()) {
				app.getMainController().loadScene(event);
			}
			break;
		case "addBtn":
			addAssociatedPart();
			break;
		case "deleteBtn":
			removeAssociatedPart();
			break;
		case "addSearchBtn":
			loadAddPartsTbl();
			break;
		case "deleteSearchBtn":
			loadDeletePartsTbl();
			break;
		}
	}
	
	/**
	 * Validate validatedTextFields and return any errors found
	 * @return string of errors
	 */
	private String getErrorMsgs() {
		String msg = "";
		final ValidatedTextField[] fields = { invTxt, priceCostTxt, maxTxt, minTxt, nameTxt };

		for (ValidatedTextField field : fields) {
			if (field.hasError()) {
				msg += field.getAlertMsg() + "\n\n";
			}
		}
		return msg.length() == 0 ? null : msg;
	}

	/**
	 * Setup GUI based on button pressed from main controller
	 * @param event
	 * @param part
	 */
	public void setMainLbl(ActionEvent event, Product product) {
		final String id = ((Control) event.getSource()).getId();
		if (id.contains("add")) {
			this.product = new Product();
			nameTxt.setText("(NEW PRODUCT)");
			nameTxt.setEffect(null);
			mainLbl.setText("Add Product");
			idTxt.setText("AUTO-INCREMENT");
			invTxt.setText("0");
			invTxt.setEffect(null);
			priceCostTxt.setText("0");
			priceCostTxt.setEffect(null);
			maxTxt.setText("0");
			maxTxt.setEffect(null);
			minTxt.setText("0");
			minTxt.setEffect(null);
			available = FXCollections.observableArrayList(app.getInventory().getAllParts());
			associated = FXCollections.observableArrayList();
			loadAllTables();
		} else {
			this.product = product;
			mainLbl.setText("Modify Product");
			idTxt.setText(String.valueOf(product.getProductId()));
			nameTxt.setText(product.getName());
			invTxt.setText(String.valueOf(product.getInStock()));
			priceCostTxt.setText(String.valueOf(product.getPrice()));
			maxTxt.setText(String.valueOf(product.getMax()));
			minTxt.setText(String.valueOf(product.getMin()));
			List<Part> allAvail = app.getInventory().getAllParts();
			List<Part> allAssociated = product.getAssociatedParts();
			allAssociated.forEach(
					nextAssoc -> allAvail.removeIf(nextAvail -> nextAvail.getPartId() == nextAssoc.getPartId()));
			available = FXCollections.observableArrayList(allAvail);
			associated = FXCollections.observableArrayList(allAssociated);
			loadAllTables();
		}
	}

	/**
	 * Save a product
	 * @return true if user is finished with product after saving
	 */
	private boolean saveProduct() {
		final String errorMsgs = getErrorMsgs();
		if (errorMsgs != null) {
			app.alert(AlertType.ERROR, "Invalid Data", errorMsgs);
			return false;
		}

		if (product.getAssociatedParts().size() == 0) {
			app.alert(AlertType.ERROR, "Unable to Save Product",
					"You must associate atleast one part to the product before you can save.");
		} else {
			final String name = nameTxt.getText();
			final int inStock = Integer.valueOf(invTxt.getText());
			final double price = Double.valueOf(priceCostTxt.getText());
			final int max = Integer.valueOf(maxTxt.getText());
			final int min = Integer.valueOf(Integer.valueOf(minTxt.getText()));

			product.setName(name);
			product.setInStock(inStock);
			product.setPrice(price);
			product.setMax(max);
			product.setMin(min);

			if (min > max || inStock > max || inStock < min || price < product.getTotalPriceOfAssociatedParts()) {
				app.alert(AlertType.ERROR, "Invalid Data",
						"Data must abide by the following rules:\n\nMin cannot be greater than max.\nInv cannot be greater than max.\nInv cannot be less than min\nProduct price must be greater than or equal to the total price of the associated parts.");
				return false;
			}

			if (product.getProductId() == 0) {
				product = app.getInventory().addProduct(product);
				idTxt.setText(String.valueOf(product.getProductId()));
			} else {
				app.getInventory().updateProduct(product);
			}
			mainLbl.setText("Modify Product");
			return app.alert(AlertType.CONFIRMATION, "Prompt", "Are you finished with this product?");
		}
		return false;
	}

	/**
	 * Add associated part to product and setup tables
	 */
	private void addAssociatedPart() {
		// Get selected part
		Part selectedAddPart = addPartsTbl.getSelectionModel().getSelectedItem();

		if (selectedAddPart == null) {
			app.alert(AlertType.ERROR, "No Part Selected",
					"Please select a part from the table to perform this action.");
			return;
		}

		// Add part to associated parts for the product and set the
		// observable list
		product.addAssociatedPart(selectedAddPart);
		associated = FXCollections.observableArrayList(product.getAssociatedParts());

		// Remove selected part from available parts, and set both tables
		available.remove(selectedAddPart);
		app.alert(AlertType.INFORMATION, "Associate Part (ID:" + selectedAddPart.getPartId() + ") Successful",
				"Id: " + selectedAddPart.getPartId() + "\nName: " + selectedAddPart.getName());
		loadAllTables();
	}

	/**
	 * Remove associated part from product and setup tables
	 */
	private void removeAssociatedPart() {
		// Get selected part
		Part selectedDeletePart = deletePartsTbl.getSelectionModel().getSelectedItem();

		if (selectedDeletePart == null) {
			app.alert(AlertType.ERROR, "No Part Selected",
					"Please select a part from the table to perform this action.");
			return;
		}

		// Delete part from associated parts for the product and set the
		// observable lists
		try {
			if (product.removeAssociatedPart(selectedDeletePart)) {
				// Set associated after item is removed from ArrayList and add
				// item back to available
				associated = FXCollections.observableArrayList(product.getAssociatedParts());
				available.add(selectedDeletePart);

				// Reload tables
				loadAllTables();

				// Alert user of action
				app.alert(AlertType.INFORMATION,
						"Disassociate Part (ID:" + selectedDeletePart.getPartId() + ") Successful",
						"Id: " + selectedDeletePart.getPartId() + "\nName: " + selectedDeletePart.getName());
			}
		} catch (Exception e) {
			app.alert(AlertType.ERROR, "Disassociate Part (ID:" + selectedDeletePart.getPartId() + ") Unsuccessful",
					e.getMessage());
		}
	}
	
	/**
	 * Load available parts table, apply filter of searchTxt if applicable
	 */
	private void loadAddPartsTbl() {
		addPartsTbl.setItems(FXCollections.observableArrayList(available.stream().filter(next -> {
			final String data = next.getPartId() + next.getName() + next.getInStock() + next.getFormattedPrice();
			return data.toLowerCase().contains(addSearchTxt.getText().toLowerCase());
		}).collect(Collectors.toList())));
	}
	
	/**
	 * Load associated parts table, apply filter of searchTxt if applicable
	 */
	private void loadDeletePartsTbl() {
		deletePartsTbl.setItems(FXCollections.observableArrayList(associated.stream().filter(next -> {
			final String data = next.getPartId() + next.getName() + next.getInStock() + next.getFormattedPrice();
			return data.toLowerCase().contains(deleteSearchTxt.getText().toLowerCase());
		}).collect(Collectors.toList())));
	}

	/*
	 * Load both available and associated part tables
	 */
	private void loadAllTables() {
		loadAddPartsTbl();
		loadDeletePartsTbl();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// Bind available parts table
		addPartIdColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("partId"));
		addPartNameColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("name"));
		addInStockColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("inStock"));
		addPriceColumn.setCellValueFactory(cellData -> Bindings.format("%.2f", cellData.getValue().getPrice()));

		// Bind associated parts table
		deletePartIdColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("partId"));
		deletePartNameColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("name"));
		deleteInStockColumn.setCellValueFactory(new PropertyValueFactory<Part, String>("inStock"));
		deletePriceColumn.setCellValueFactory(cellData -> Bindings.format("%.2f", cellData.getValue().getPrice()));
		
		// Bind change listeners for auto-searching of tables
		addSearchTxt.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				loadAddPartsTbl();
			}
		});
		
		deleteSearchTxt.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> ov, String t, String t1) {
				loadDeletePartsTbl();
			}
		});
	}
}
