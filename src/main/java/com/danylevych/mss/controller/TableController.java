package com.danylevych.mss.controller;

import static com.danylevych.mss.util.CollectionUtils.joinAndWrap;

import com.danylevych.mss.model.PCB;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class TableController {

    @FXML
    private TableView<PCB> table;
    @FXML
    private TableColumn<PCB, String> pid;
    @FXML
    private TableColumn<PCB, String> tat;
    @FXML
    private TableColumn<PCB, String> ioBursts;
    @FXML
    private TableColumn<PCB, String> cpuBursts;
    @FXML
    private TableColumn<PCB, Integer> waitTime;
    @FXML
    private TableColumn<PCB, String> responseTime;
    @FXML
    private TableColumn<PCB, Integer> arrivalTime;

    public void setData(ObservableList<PCB> data) {
        table.setItems(data);

        pid.setCellValueFactory(pcb -> pcb.getValue().nameProperty());
        tat.setCellValueFactory(pcb -> pcb.getValue().tatProperty());

        arrivalTime.setCellValueFactory(
                pcb -> pcb.getValue().getArrivalTimeProperty());

        waitTime.setCellValueFactory(
                pcb -> pcb.getValue().waitingTimeProperty());

        responseTime.setCellValueFactory(
                pcb -> pcb.getValue().responseTimeProperty());

        cpuBursts.setCellValueFactory(
                pcb -> joinAndWrap(pcb.getValue().getCpuBurstsTime()));

        ioBursts.setCellValueFactory(
                pcb -> joinAndWrap(pcb.getValue().getIoBurstsTime()));
    }

}
