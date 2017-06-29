/**
 * Sapelli data collection platform: http://sapelli.org
 * <p>
 * Copyright 2012-2016 University College London - ExCiteS group
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.ac.ucl.excites.sapelli.packager;

import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import uk.ac.ucl.excites.sapelli.packager.ui.Controller;

/**
 * Entry point for the Packager Javafx application. This class setups the UI and launches the app.
 * <p>
 * Created by Michalis on 25/05/2017.
 */
@Slf4j
public class Packager extends Application
{
	@Override
	public void start(Stage primaryStage) throws Exception
	{
		final ResourceBundle bundle = ResourceBundle.getBundle("LanguageBundle");
		final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/PackagerUI.fxml"), bundle);
		final Parent root = fxmlLoader.load();
		final Scene scene = new Scene(root);
		primaryStage.setTitle(bundle.getString("app"));
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image("/icon/SapelliPackager.png"));
		primaryStage.show();

		// Call the Controller close method when the stage is about to close and clean up resources
		primaryStage.setOnCloseRequest(event ->
		  {
			  Controller controller = fxmlLoader.getController();
			  controller.close();
		  }
		);
	}

	public static void main(String[] args)
	{
		log.info("Sapelli Packer is starting!");

		try
		{
			launch(args);
		}
		catch(Exception e)
		{
			log.error("Error while trying to load JavaFX: ", e);
		}
	}
}
