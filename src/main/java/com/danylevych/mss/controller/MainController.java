package com.danylevych.mss.controller;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.TRANSPARENT;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.ProcessQueue;
import com.danylevych.mss.model.event.Event;
import com.danylevych.mss.model.event.EventListener;
import com.danylevych.mss.view.MainView;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class MainController extends MainView implements EventListener {

    @FXML
    private void onJobsInfoSelect() {
        if (tableWindow.isShowing()) {
            tableWindow.hide();
        } else {
            tableWindow.show();
        }
    }

    @FXML
    private void onPause() {
        computer.setStepMode(true);
    }

    @FXML
    private void onStep() {
        computer.setStepMode(true);
        computer.run();
    }

    @FXML
    private void onRun() {
        computer.setStepMode(false);
        computer.run();
    }

    @FXML
    private void onSolve() {
        //
    }

    @Override
    public void handle(Event event, int param) {
        switch (event) {
        case ADD_JOB -> addJob(param);
        case SET_CLOCK -> setClock(param);
        case INTERRUPT -> interrupt(param);
        case IO_REQUEST -> addIoJob(param);
        case JOB_DONE -> completeJob(param);
        case IO_DONE -> completeIoJob(param);
        case JOB_ASSIGNED -> assignJob(param);
        }
    }

    private void assignJob(int cpuId) {
        if (cpuId == -1) {
            String pName = computer.getIoDevice().getCurrentJob().getName();
            highlight(jobShapes.remove(pName), () -> {
                ioQueue.getChildren().remove(0);
                highlight(setIoState(pName));
            });
        } else {
            String pName = computer.getCpu(cpuId).getCurrentJob().getName();
            highlight(jobShapes.remove(pName), () -> {
                queueHBoxs.get(cpuId).getChildren().remove(0);
                highlight(setCpuState(pName, cpuId));
            });
        }
    }

    private void addIoJob(int cpuId) {
        highlight(setCpuState(IDLE, cpuId), () -> {
            PCB job = computer.getCpu(cpuId).getCurrentJob();
            int pos = computer.getIoWaitingQueue().indexOf(job);
            highlight(addJob(ioQueue, job.getName(), pos));
        });
    }

    private void completeIoJob(int cpuId) {
        highlight(setIoState(IDLE), () -> highlight(addJob(cpuId)));
    }

    private void interrupt(int cpuId) {
        highlight(setCpuState(IDLE, cpuId), () -> highlight(addJob(cpuId)));
    }

    private void completeJob(int cpuId) {
        highlight(setCpuState(IDLE, cpuId));
    }

    private Shape addJob(int cpuId) {
        int pos = computer.getSheduler().getLastAddedJobPos();
        ProcessQueue queue = computer.getSheduler().getReadyQueue(cpuId);
        return addJob(queueHBoxs.get(cpuId), queue.get(pos).getName(), pos);
    }

    private Shape addJob(HBox queue, String pName, int pos) {
        Shape jobShape = new Rectangle(25.0, 25.0, TRANSPARENT);
        jobShape.setStroke(BLACK);

        jobShapes.put(pName, jobShape);

        StackPane job = new StackPane(jobShape, new Label(pName));
        queue.getChildren().add(pos, job);

        return jobShape;
    }

    private void setClock(int clock) {
        Task<Void> pause = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Thread.sleep(1000L);
                Platform.runLater(() -> clockLabel.setFill(BLACK));
                return null;
            }
        };

        pause.setOnRunning(e -> clockLabel.setFill(RED));
        pause.setOnSucceeded(e -> {
            clockLabel.setText(String.valueOf(clock));
            continueSimulation();
        });
        new Thread(pause).start();
    }

    private void highlight(Shape shape) {
        highlight(shape, this::continueSimulation);
    }

    private static void highlight(Shape shape, Runnable runAfter) {
        Task<Void> highlight = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                shape.setStroke(RED);
                Thread.sleep(1000L);
                shape.setStroke(BLACK);
                runAfter.run();
                return null;
            }
        };

        new Thread(highlight).start();
    }

    private void continueSimulation() {
        computer.getCurrentTask().interrupt();
    }

    private Circle setIoState(String state) {
        ioLabel.setText(state);
        return ioCircle;
    }

    private Circle setCpuState(String state, int cpuId) {
        cpuLabels.get(cpuId).setText(state);
        return cpuCircles.get(cpuId);
    }

}
