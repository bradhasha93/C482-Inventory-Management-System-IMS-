package com.ims.controllers;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public abstract class Controller {

	private String file;
	private FXMLLoader loader;
	private Scene scene;

	public abstract void handleClick(ActionEvent event);

	public Controller(String file) {
		this.file = file;
		loader = new FXMLLoader(getClass().getClassLoader().getResource(getFile()));
		loader.setController(this);
		try {
			scene = new Scene(loader.load());
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Get controller file name
	 * @return
	 */
	public String getFile() {
		return this.file;
	}

	public void loadScene(ActionEvent event) {
		loadScene(event, null);
	}

	public void loadScene(Stage stage) {
		loadScene(null, stage);
	}

	/**
	 * Load correct scene using event or current stage
	 * @param event
	 * @param stage
	 */
	private void loadScene(ActionEvent event, Stage stage) {
		// Determine originating source
		Stage app_stage = event != null ? (Stage) ((Node) event.getSource()).getScene().getWindow() : stage;

		// Hide current scene if one is showing
		if (app_stage.isShowing())
			app_stage.hide();

		// Set stages scene
		app_stage.setScene(scene);
		app_stage.show();
	}

}
