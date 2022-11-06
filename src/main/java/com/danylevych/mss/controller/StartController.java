package com.danylevych.mss.controller;

import static com.danylevych.mss.util.ProcessQueueUtils.generateEvents;
import static com.danylevych.mss.util.ProcessQueueUtils.readInputFile;
import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.IOException;

import com.danylevych.mss.App;
import com.danylevych.mss.model.Computer;
import com.danylevych.mss.model.ProcessQueue;
import com.danylevych.mss.model.sheduler.Sheduler;
import com.danylevych.mss.model.sheduler.impl.FCFS;

import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartController {
    private static final String RANDOM = "Генерація випадкових даних";
    private static final String FILE = "Вихідний файл";

    private static final String FCFS = "FCFS";
    private static final String RR = "RR";

    private boolean hasGlobalQueue;
    private int quantum;
    private int nCpu;

    @FXML
    protected ChoiceBox<String> algorithm;
    @FXML
    protected ChoiceBox<String> inputMethod;

    @FXML
    protected RadioButton hasDistibutedQueues;

    @FXML
    protected Button chooseFileButton;
    @FXML
    protected TextField chooseFileInput;

    @FXML
    protected TextField nCpuInput;

    @FXML
    protected Label nEventsLabel;
    @FXML
    protected TextField nEventsInput;

    @FXML
    protected Label quantumLabel;
    @FXML
    protected TextField quantumInput;

    @FXML
    private void initialize() {
        String curDir = System.getProperty("user.dir");
        chooseFileInput.setText(curDir + "\\input.txt");

        inputMethod.valueProperty().addListener(this::handleInputMethodChange);
        inputMethod.getItems().addAll(FILE, RANDOM);
        inputMethod.setValue(FILE);

        algorithm.valueProperty().addListener(this::handleAlgorithmChange);
        algorithm.getItems().addAll(FCFS, RR);
        algorithm.setValue(FCFS);
    }

    @FXML
    private void onStartSimulation() throws IOException {
        ProcessQueue jobs = getEvents(inputMethod.getValue());
        Sheduler sheduler = getSheduler(algorithm.getValue());
        Computer computer = new Computer(jobs, sheduler, nCpu, quantum);

        FXMLLoader fxmlLoader = App.setScene("Main.fxml");
        MainController mainController = fxmlLoader.getController();
        mainController.setComputer(computer);
    }

    @FXML
    private void onChooseFile() {
        File file = new FileChooser().showOpenDialog(new Stage());
        if (file != null) {
            chooseFileInput.setText(file.getPath());
        }
    }

    private ProcessQueue getEvents(String inputMethod) {
        return switch (inputMethod) {
        case RANDOM -> generateEvents(parseInt(nEventsInput.getText()));
        case FILE -> readInputFile(chooseFileInput.getText());
        default -> throw new IllegalArgumentException(inputMethod);
        };
    }

    private Sheduler getSheduler(String algorithm) {
        nCpu = parseInt(nCpuInput.getText());
        hasGlobalQueue = !hasDistibutedQueues.isSelected();

        return switch (algorithm) {
        case FCFS -> {
            quantum = -1;
            yield createFCSF();
        }
        case RR -> {
            quantum = parseInt(quantumInput.getText());
            yield createFCSF();
        }
        default -> throw new IllegalArgumentException(algorithm);
        };
    }

    private Sheduler createFCSF() {
        return new FCFS(hasGlobalQueue, nCpu);
    }

    private void handleInputMethodChange(
            ObservableValue<? extends String> o,
            String oldValue,
            String inputMethod) {
        final boolean shouldEnterEventsNumber = RANDOM.equals(inputMethod);
        nEventsLabel.setVisible(shouldEnterEventsNumber);
        nEventsInput.setVisible(shouldEnterEventsNumber);
        chooseFileButton.setVisible(!shouldEnterEventsNumber);
        chooseFileInput.setVisible(!shouldEnterEventsNumber);
    }

    private void handleAlgorithmChange(
            ObservableValue<? extends String> o,
            String oldValue,
            String algorithm) {
        final boolean shouldEnterQuantum = RR.equals(algorithm);
        quantumLabel.setVisible(shouldEnterQuantum);
        quantumInput.setVisible(shouldEnterQuantum);
    }

}
