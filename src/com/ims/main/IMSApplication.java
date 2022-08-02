package com.ims.main;

import java.util.Optional;

import com.ims.content.Inventory;
import com.ims.controllers.IMSAddModifyPartController;
import com.ims.controllers.IMSAddModifyProductController;
import com.ims.controllers.IMSMainController;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class IMSApplication extends Application {

	private IMSMainController mainController = new IMSMainController(this);
	private IMSAddModifyProductController productController = new IMSAddModifyProductController(this);
	private IMSAddModifyPartController partController = new IMSAddModifyPartController(this);
	private IMSDatabase database = new IMSDatabase();
	private Inventory inventory = new Inventory(this);

	public IMSApplication() throws Exception {
		init();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("C482 Performance Assessment");
		mainController.loadScene(primaryStage);

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						exit();
					}
				});
			}
		});
	}

	/**
	 * Return application's instance of main controller
	 * 
	 * @return mainController
	 */
	public IMSMainController getMainController() {
		return this.mainController;
	}

	/**
	 * Return application's instance of productController
	 * 
	 * @return productController
	 */
	public IMSAddModifyProductController getProductController() {
		return this.productController;
	}

	/**
	 * Return application's instance of partController
	 * 
	 * @return partController
	 */
	public IMSAddModifyPartController getPartController() {
		return this.partController;
	}

	/**
	 * Return application's instance of the database
	 * 
	 * @return database
	 */
	public IMSDatabase getDatabase() {
		return this.database;
	}

	/**
	 * Return application's instance of inventory
	 * 
	 * @return inventory
	 */
	public Inventory getInventory() {
		return this.inventory;
	}

	/**
	 * Close database connection and exit application
	 */
	public void exit() {
		database.close();
		System.out.println("Application closed.");
		System.exit(0);
	}

	/**
	 * Handle prompts for info, warning, confirmation, and error
	 * 
	 * @param type
	 * @param title
	 * @param content
	 * @return prompt
	 */
	public boolean alert(AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setHeaderText(title);

		switch (type) {
		case CONFIRMATION:
		case INFORMATION:
		case NONE:
		case WARNING:
			alert.setContentText(content);
			break;
		case ERROR:
			// Create expandable Exception.
			Label label = new Label("The exception stacktrace was:");

			TextArea textArea = new TextArea(content);
			textArea.setEditable(false);
			textArea.setWrapText(true);

			textArea.setMaxWidth(Double.MAX_VALUE);
			textArea.setMaxHeight(Double.MAX_VALUE);
			GridPane.setVgrow(textArea, Priority.ALWAYS);
			GridPane.setHgrow(textArea, Priority.ALWAYS);

			GridPane expContent = new GridPane();
			expContent.setMaxWidth(Double.MAX_VALUE);
			expContent.add(label, 0, 0);
			expContent.add(textArea, 0, 1);

			// Set expandable Exception into the dialog pane.
			alert.getDialogPane().setExpandableContent(expContent);
			break;

		default:
			break;

		}
		Optional<ButtonType> bt = alert.showAndWait();
		return type.equals(AlertType.CONFIRMATION) ? bt.get() == ButtonType.OK : false;
	}
}
