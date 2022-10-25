package com.danylevych.mss.controller;

import static javafx.scene.paint.Color.BLACK;
import static javafx.scene.paint.Color.RED;
import static javafx.scene.paint.Color.TRANSPARENT;

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
    public void handle(Event event) {
        switch (event) {
        case CLOCK_INC -> incrementClock();
        case JOB_ASSIGNED -> assignJob();
        case JOB_DONE -> completeJob();
        case INTERRUPT -> interrupt();
        case IO_REQUEST -> addIoJob();
        case IO_DONE -> completeIoJob();
        case ADD_JOB -> throw new UnsupportedOperationException();
        }
    }

    private void assignJob(int cpuId) {
        if (cpuId == -1) {
            String pName = computer.getIoDevice().getCurrentJob().getName();

            highlight(jobRectangles.remove(pName), () -> {
                ioQueue.getChildren().remove(0);

                highlight(ioCircle, () -> {
                    ioLabel.setText(pName);
                    continueSimulation();
                });
            });
        } else {
            String pName = computer.getCpu(cpuId).getCurrentJob().getName();

            highlight(jobRectangles.remove(pName), () -> {
                int i = computer.getSheduler().hasGlobalQueue() ? 0 : cpuId;
                queueHBoxs.get(i).getChildren().remove(0);

                highlight(cpuCircles.get(cpuId), () -> {
                    cpuLabels.get(cpuId).setText(pName);
                    continueSimulation();
                });
            });
        }
    }

    private void addIoJob(int cpuId) {
        highlight(cpuCircles.get(cpuId), () -> {
            cpuLabels.get(cpuId).setText(IDLE);

            String pName = computer.getIoWaitingQueue().getLast().getName();

            highlight(addJob(ioQueue, pName), this::continueSimulation);
        });
    }

    private void completeIoJob(int queueId) {
        highlight(ioCircle, () -> {
            ioLabel.setText(IDLE);
            highlight(addJob(queueId), this::continueSimulation);
        });
    }

    private void interrupt(int cpuId) {
        highlight(cpuCircles.get(cpuId), () -> {
            cpuLabels.get(cpuId).setText(IDLE);
            highlight(addJob(cpuId), this::continueSimulation);
        });
    }

    private void completeJob(int cpuId) {
        highlight(cpuCircles.get(cpuId), () -> {
            cpuLabels.get(cpuId).setText(IDLE);
            continueSimulation();
        });
    }

    private Rectangle addJob(int queueId) {
        HBox queueHbox = queueHBoxs.get(queueId);
        ProcessQueue queue = computer.getSheduler().getReadyQueue(queueId);
        String pName = queue.getLast().getName();
        return addJob(queueHbox, pName);
    }

    private Rectangle addJob(HBox queue, String pName) {
        Label jobName = new Label(pName);

        Rectangle jobShape = new Rectangle(25.0, 25.0, TRANSPARENT);
        jobShape.setStroke(BLACK);

        jobRectangles.put(pName, jobShape);

        StackPane job = new StackPane(jobShape, jobName);
        queue.getChildren().add(job);

        return jobRectangles.get(pName);
    }

    private static void highlight(Shape shape, Runnable runAfter) {
        Task<Void> pause = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                shape.setStroke(RED);
                Thread.sleep(1000L);
                shape.setStroke(BLACK);
                runAfter.run();
                return null;
            }
        };

        new Thread(pause).start();
    }

    private void incrementClock() {
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
            clockLabel.setText(String.valueOf(computer.getClock()));
            continueSimulation();
        });
        new Thread(pause).start();
    }

    private void continueSimulation() {
        computer.getCurrentTask().interrupt();
    }
}
