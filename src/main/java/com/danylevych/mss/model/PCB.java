package com.danylevych.mss.model;

import static com.danylevych.mss.util.PerfEval.responseTime;
import static com.danylevych.mss.util.PerfEval.tat;

import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PCB {

    private final IntegerProperty waitingTime;
    private final StringProperty responseTime;
    private final StringProperty name;
    private final StringProperty tat;

    private final List<Integer> cpuBurstsTime;
    private final List<Integer> ioBurstsTime;

    private final int arrivalTime;
    private int timeLeft;

    public PCB(int pid, int arrivalTime, List<Integer> cpuBurstsTime,
            List<Integer> ioBurstsTime) {

        this.name = new SimpleStringProperty("П" + pid);
        this.waitingTime = new SimpleIntegerProperty();
        this.responseTime = new SimpleStringProperty();
        this.tat = new SimpleStringProperty();

        this.cpuBurstsTime = cpuBurstsTime;
        this.ioBurstsTime = ioBurstsTime;

        this.timeLeft = cpuBurstsTime.get(0);
        this.arrivalTime = arrivalTime;
    }

    public final StringProperty tatProperty() {
        return tat;
    }

    public final StringProperty responseTimeProperty() {
        return responseTime;
    }

    public final IntegerProperty waitingTimeProperty() {
        return waitingTime;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.getValue();
    }

    public Integer getWaitingTime() {
        return waitingTime.getValue();
    }

    public List<Integer> getCpuBurstsTime() {
        return cpuBurstsTime;
    }

    public List<Integer> getIoBurstsTime() {
        return ioBurstsTime;
    }

    public void setFirstRunTime(int t) {
        responseTime.set(String.valueOf(responseTime(t, arrivalTime)));
    }

    public void setFinishTime(int t) {
        tat.set(String.valueOf(tat(t, arrivalTime)));
    }

    public boolean nextIoBurst() {
        if (ioBurstsTime.isEmpty()) {
            return false;
        }
        timeLeft = ioBurstsTime.remove(0);
        return true;
    }

    public void nextCpuBurst() {
        timeLeft = cpuBurstsTime.remove(0);
    }

    public void incrementWaitingTime() {
        waitingTime.set(waitingTime.get() + 1);
    }

    public boolean hasNotRunYet() {
        return responseTime.get() == null;
    }

    public boolean decrementTimeleft() {
        return --timeLeft == 0;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

}
