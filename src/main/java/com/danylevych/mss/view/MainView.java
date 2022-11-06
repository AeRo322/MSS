package com.danylevych.mss.view;

import static java.lang.Math.ceil;
import static javafx.geometry.NodeOrientation.RIGHT_TO_LEFT;
import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.TRANSPARENT;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.danylevych.mss.App;
import com.danylevych.mss.controller.TableController;
import com.danylevych.mss.model.Computer;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class MainView {

    protected static final String IDLE = "Простій";

    protected Stage tableWindow = new Stage();

    protected Map<String, Shape> jobShapes = new HashMap<>();
    protected List<Circle> cpuCircles = new LinkedList<>();
    protected List<Label> cpuLabels = new LinkedList<>();
    protected List<HBox> queueHBoxs = new LinkedList<>();

    protected Circle ioCircle = deviceCircle();
    protected Label ioLabel = new Label(IDLE);
    protected HBox ioQueue = new HBox();

    @FXML
    private Button startButton;
    @FXML
    private Button pauseButton;
    @FXML
    private Button stepButton;
    @FXML
    private Button solveButton;

    @FXML
    protected VBox readyQueuesView;
    @FXML
    protected TilePane cpuView;
    @FXML
    protected TilePane ioView;
    @FXML
    protected VBox ioQueuesView;

    @FXML
    protected Text clockLabel;

    protected Computer computer;

    public void setComputer(Computer computer) throws IOException {
        this.computer = computer;
        
        computer.runningProperty().addListener(this::handleRunStateChange);
        computer.isDoneProperty().addListener(this::handleIsDoneChange);

        createCpuView();
        createQueuesView();
        createIoView();

        FXMLLoader fxmlLoader = App.setScene("Table.fxml", tableWindow);
        TableController tableController = fxmlLoader.getController();
        tableController.setData(computer.getJobs());

        tableWindow.setTitle("Таблиця процесів");
        tableWindow.show();
    }

    private void createQueuesView() {
        final int cpuCount = computer.cpuCount();
        if (computer.getSheduler().hasGlobalQueue()) {
            Label queueName = new Label("Глобальна черга");
            createCpuQueue(queueName);
            for (int i = 0; i < cpuCount; i++) {
                queueHBoxs.add(queueHBoxs.get(0));
            }
        } else {
            for (int i = 0; i < cpuCount; i++) {
                Label queueName = new Label("ЦП " + (i + 1));
                createCpuQueue(queueName);
            }
        }

        ioQueuesView.getChildren().add(ioQueue);
    }

    private void createCpuQueue(Label queueName) {
        HBox queueHBox = new HBox();
        queueHBox.setNodeOrientation(RIGHT_TO_LEFT);

        queueHBoxs.add(queueHBox);

        readyQueuesView.getChildren().add(queueName);
        readyQueuesView.getChildren().add(queueHBox);
    }

    private void createIoView() {
        StackPane ioDevice = new StackPane(ioCircle, ioLabel);
        ioView.getChildren().add(ioDevice);
    }

    private void createCpuView() {
        final int cpuCount = computer.cpuCount();
        for (int i = 0; i < cpuCount; i++) {
            Circle circle = deviceCircle();
            cpuCircles.add(circle);

            Label cpuLabel = new Label(IDLE);
            cpuLabels.add(cpuLabel);

            cpuView.getChildren().add(new StackPane(circle, cpuLabel));
        }

        cpuView.setPrefColumns(idkMan(cpuCount));
    }

    private static Circle deviceCircle() {
        Circle circle = new Circle(25.0, TRANSPARENT);
        circle.setStroke(BLACK);
        return circle;
    }

    private static int idkMan(int cpuCount) {
        double lmao;
        if (cpuCount >= 16) {
            lmao = 4.0;
        } else if (cpuCount >= 12) {
            lmao = 3.0;
        } else {
            lmao = 2.0;
        }
        return (int) ceil(cpuCount / lmao);
    }

    private void handleRunStateChange(
            ObservableValue<? extends Boolean> o,
            Boolean oldValue,
            Boolean isRunning) {
        startButton.setDisable(isRunning);
        pauseButton.setDisable(!isRunning);
    }

    private void handleIsDoneChange(
            ObservableValue<? extends Boolean> o,
            Boolean oldValue,
            Boolean isDone) {
        solveButton.setDisable(isDone);
        startButton.setDisable(isDone);
    }

}
