package com.danylevych.mss;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static List<Runnable> onExitTasks = new ArrayList<>();
    private static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws IOException {
        setPrimaryStage(primaryStage);
        setScene("Start.fxml");

        primaryStage.setTitle("MSS: Multiprocessor Scheduling Simulator");
        primaryStage.show();
    }

    @Override
    public void stop() {
        onExitTasks.forEach(Runnable::run);
    }

    public static void addOnExit(Runnable task) {
        onExitTasks.add(task);
    }

    public static FXMLLoader setScene(String fxml) throws IOException {
        return setScene(fxml, primaryStage);
    }

    public static FXMLLoader setScene(String fxml, Stage stage)
            throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml));
        stage.setScene(new Scene(fxmlLoader.load()));
        return fxmlLoader;
    }

    private static void setPrimaryStage(Stage primaryStage) {
        App.primaryStage = primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
