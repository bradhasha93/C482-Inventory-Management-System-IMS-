package com.ims.controllers;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.ims.content.Inhouse;
import com.ims.content.Outsourced;
import com.ims.content.Part;
import com.ims.content.Product;
import com.ims.content.ValidatedTextField;
import com.ims.main.IMSApplication;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class IMSAddModifyPartController extends Controller implements Initializable {

	private IMSApplication app;
	private Part part;

	// Labels
	@FXML
	private Label mainLbl, specialLbl;

	// Textfields
	@FXML
	private TextField idTxt;

	@FXML
	private ValidatedTextField nameTxt, invTxt, minTxt, priceCostTxt, maxTxt, specialTxt;

	// Buttons
	@FXML
	private Button cancelBtn, saveBtn;

	// Radio buttons
	@FXML
	private RadioButton inHouseBtn, outsourcedBtn;

	public IMSAddModifyPartController(IMSApplication app) {
		super("IMSAddModifyPart.fxml");
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
		case "inHouseBtn":
			setForInhouse();
			break;
		case "outsourcedBtn":
			setForOutsourced();
			break;
		case "saveBtn":
			if (savePart()) {
				app.getMainController().loadScene(event);
			}
			break;
		}
	}

	/**
	 * Setup GUI for inHouse
	 */
	private void setForInhouse() {
		specialLbl.setText("Machine ID");
		specialTxt.setMask("^[0-9]*$");
		specialTxt.setAlertMsg("Field (Machine ID) can only accept integer values.");
		specialTxt.setText("0");
		specialTxt.setEffect(null);
	}

	/**
	 * Set GUI for outsourced
	 */
	private void setForOutsourced() {
		specialLbl.setText("Company Name");
		specialTxt.setMask(".*");
		specialTxt.setText("");
		specialTxt.setEffect(null);
	}

	/**
	 * Validate validatedTextFields and return any errors found
	 * @return string of errors
	 */
	private String getErrorMsgs() {
		String msg = "";
		final ValidatedTextField[] fields = { invTxt, priceCostTxt, maxTxt, minTxt, specialTxt, nameTxt };

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
	public void setMainLbl(ActionEvent event, Part part) {
		final String id = ((Control) event.getSource()).getId();
		if (id.contains("add")) {
			this.part = null;
			nameTxt.setText("(NEW PART)");
			nameTxt.setEffect(null);
			mainLbl.setText("Add Part");
			inHouseBtn.setSelected(true);
			idTxt.setText("AUTO-INCREMENT");
			idTxt.setEffect(null);
			priceCostTxt.setText("0");
			priceCostTxt.setEffect(null);
			invTxt.setText("0");
			invTxt.setEffect(null);
			minTxt.setText("0");
			minTxt.setEffect(null);
			maxTxt.setText("0");
			maxTxt.setEffect(null);
			specialLbl.setText("Machine ID");
			setForInhouse();
			specialTxt.setText("0");
		} else {
			this.part = part;
			mainLbl.setText("Modify Part");
			if (part instanceof Inhouse) {
				inHouseBtn.setSelected(true);
				setForInhouse();
				specialLbl.setText("Machine ID");
				specialTxt.setText(String.valueOf(((Inhouse) part).getMachineId()));
			} else {
				outsourcedBtn.setSelected(true);
				setForOutsourced();
				specialLbl.setText("Company Name");
				specialTxt.setText(String.valueOf(((Outsourced) part).getCompanyName()));
			}
			idTxt.setText(String.valueOf(part.getPartId()));
			nameTxt.setText(part.getName());
			invTxt.setText(String.valueOf(part.getInStock()));
			priceCostTxt.setText(part.getFormattedPrice());
			maxTxt.setText(String.valueOf(part.getMax()));
			minTxt.setText(String.valueOf(part.getMin()));
		}
	}

	/**
	 * Save part
	 * @return true if user is finished with part after saving
	 */
	private boolean savePart() {
		final String errorMsgs = getErrorMsgs();
		if (errorMsgs != null) {
			app.alert(AlertType.ERROR, "Invalid Data", errorMsgs);
			return false;
		}

		if (part == null) {
			if (inHouseBtn.isSelected()) {
				part = new Inhouse();
				((Inhouse) part).setMachineId(Integer.valueOf(specialTxt.getText()));
			} else {
				part = new Outsourced();
				((Outsourced) part).setCompanyName(specialTxt.getText());
			}
		}

		final String name = nameTxt.getText();
		final int inStock = Integer.valueOf(invTxt.getText());
		final double price = Double.valueOf(priceCostTxt.getText());
		final int max = Integer.valueOf(maxTxt.getText());
		final int min = Integer.valueOf(Integer.valueOf(minTxt.getText()));

		part.setName(name);
		part.setInStock(inStock);
		part.setPrice(price);
		part.setMax(max);
		part.setMin(min);

		if (min > max || inStock > max || inStock < min) {
			app.alert(AlertType.ERROR, "Invalid Data",
					"Data must abide by the following rules:\n\nMin cannot be greater than max.\nInv cannot be greater than max.\nInv cannot be less than min.");
			return false;
		}

		if (part.getPartId() == 0) {
			part = app.getInventory().addPart(part);
		} else {
			String failedProductChecks = "";
			List<Product> products = app.getInventory().getAllAssociatedProducts(part);
			final double partPriceChange = app.getInventory().lookupPart(part.getPartId()).getPrice() - part.getPrice();

			for (Product product : products) {
				final double allAssociatedPartsTotal = product.getTotalPriceOfAssociatedParts();
				final double productPriceVariance = (product.getPrice() - allAssociatedPartsTotal) + partPriceChange;
				if (productPriceVariance < 0) {
					failedProductChecks += product.getProductId() + ",";
				}
			}
			failedProductChecks = failedProductChecks.replaceAll(",$", "");

			if (failedProductChecks.length() > 0) {
				app.alert(AlertType.ERROR, "Unable to Modify Part (ID:" + part.getPartId() + ")",
						"The resulting price change would cause the product(s) listed below's price to be less than the total price of the associated parts.  You must edit the product(s) price to account for the part price change.\n\nProduct IDs: "
								+ failedProductChecks);
				return false;
			}
			app.getInventory().updatePart(part);
		}
		mainLbl.setText("Modify Part");
		return app.alert(AlertType.CONFIRMATION, "Prompt", "Are you finished with this part?");
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}
