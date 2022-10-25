package com.danylevych.mss;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        App.primaryStage = primaryStage; // NOSONAR: Singleton
        App.setScene("Start.fxml");

        primaryStage.setTitle("MSS: Multiprocessor Scheduling Simulator");
        primaryStage.show();
    }

    public static FXMLLoader setScene(String fxml, Stage stage)
            throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        stage.setScene(new Scene(fxmlLoader.load()));
        return fxmlLoader;
    }

    public static FXMLLoader setScene(String fxml) throws IOException {
        return setScene(fxml, primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }

}