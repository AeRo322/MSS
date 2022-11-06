package com.danylevych.mss.model;

import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.List;

import com.danylevych.mss.model.event.Event;
import com.danylevych.mss.model.event.EventListener;
import com.danylevych.mss.model.sheduler.Sheduler;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class Computer {

    private final ProcessQueue ioWaitingQueue = new ProcessQueue();
    private final List<EventListener> listeners = new ArrayList<>();
    private final IoDevice ioDevice = new IoDevice();

    private final ObservableList<PCB> jobs;
    private Thread currentTask;

    private final Sheduler sheduler;
    private final int jobsToDo;
    private final int quantum;

    private final CPU[] cpus;
    private final int nCpu;

    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final BooleanProperty isDone = new SimpleBooleanProperty(false);
    private boolean isStepMode;
    private int jobsDone;
    private int clock;

    public Computer(ProcessQueue jobs, Sheduler sheduler, int nCpu,
            int quantum) {
        this.jobs = observableArrayList(jobs);
        this.jobsToDo = jobs.size();
        this.sheduler = sheduler;
        this.quantum = quantum;

        this.nCpu = nCpu;
        this.cpus = new CPU[nCpu];
        for (int i = 0; i < nCpu; i++) {
            cpus[i] = new CPU();
        }
    }

    public Computer(ProcessQueue jobs, Sheduler sheduler, int nCpu) {
        this(jobs, sheduler, nCpu, -1);
    }

    public void addEventListener(EventListener listener) {
        this.listeners.add(listener);
    }

    public void run() {
        running.set(true);
        if (listeners.isEmpty()) {
            work();
        } else {
            currentTask = new Thread(this::work);
            currentTask.start();
        }
    }

    private void work() {
        isDone.set(jobsToDo == jobsDone);

        while (!isDone.get()) {

            addJobs();
            handleInterrupts();
            assignCpuJobs();
            execCpuJobs();
            assignIoJob();
            execIoJob();
            incrementClock();

            if (isStepMode) {
                break;
            }
        }

        running.set(false);
    }

    private void addJobs() {
        for (PCB job : jobs) {
            if (job.getArrivalTime() == clock) {
                fireEvent(Event.ADD_JOB, sheduler.addJob(job));
            }
        }
    }

    private void handleInterrupts() {
        if (isTimeToInterrupt()) {
            for (int i = 0; i < nCpu; i++) {
                CPU cpu = cpus[i];
                if (cpu.isActive() && sheduler.hasNextJob(i)) {
                    final int queue = sheduler.addJob(cpu.setIdle());
                    fireEvent(Event.INTERRUPT, queue);
                }
            }
        }
    }

    private boolean isTimeToInterrupt() {
        return areInterruptsEnabled() && clock % quantum == 0;
    }

    private boolean areInterruptsEnabled() {
        return quantum != -1;
    }

    private void assignCpuJobs() {
        for (int i = 0; i < nCpu; i++) {
            CPU cpu = cpus[i];
            if (cpu.isIdle() && sheduler.hasNextJob(i)) {
                PCB nextJob = sheduler.nextJob(i);
                if (nextJob.hasNotRunYet()) {
                    nextJob.setFirstRunTime(clock);
                }
                cpu.assignJob(nextJob);
                fireEvent(Event.JOB_ASSIGNED, i);
            }
        }

        incrementWaitingTime();
    }

    private void incrementWaitingTime() {
        if (sheduler.hasGlobalQueue()) {
            sheduler.getGlobalQueue().forEach(PCB::incrementWaitingTime);
        } else {
            for (ProcessQueue queue : sheduler.getReadyQueues()) {
                queue.forEach(PCB::incrementWaitingTime);
            }
        }
    }

    private void execCpuJobs() {
        for (int i = 0; i < nCpu; i++) {
            if (cpus[i].incrementRuntime()) {
                processCpuBurstTimeleftDecrement(i);
            }
        }
    }

    private void processCpuBurstTimeleftDecrement(int cpuId) {
        CPU cpu = cpus[cpuId];
        PCB currentJob = cpu.getCurrentJob();
        if (currentJob.decrementTimeleft()) {
            if (currentJob.nextIoBurst()) {
                ioWaitingQueue.add(currentJob);
                fireEvent(Event.IO_REQUEST, cpuId);
            } else {
                jobsDone++;
                currentJob.setFinishTime(clock + 1);
                fireEvent(Event.JOB_DONE, cpuId);
            }
            cpu.setIdle();
        }
    }

    private void assignIoJob() {
        if (!ioWaitingQueue.isEmpty() && ioDevice.isIdle()) {
            ioDevice.setCurrentJob(ioWaitingQueue.removeFirst());
            fireEvent(Event.JOB_ASSIGNED, -1);
        }
    }

    private void execIoJob() {
        if (ioDevice.isActive()) {
            processIoBurstTimeleftDecrement();
        }
    }

    private void processIoBurstTimeleftDecrement() {
        PCB currentJob = ioDevice.getCurrentJob();
        if (currentJob.decrementTimeleft()) {
            currentJob.nextCpuBurst();
            fireEvent(Event.IO_DONE, sheduler.addJob(currentJob));
            ioDevice.setIdle();
        }
    }

    private void incrementClock() {
        clock++;
        fireEvent(Event.SET_CLOCK, clock);
    }

    private void fireEvent(Event event, int param) {
        if (!listeners.isEmpty()) {
            Platform.runLater(() -> notify(event, param));

            try {
                currentTask.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void notify(Event event, int param) {
        for (EventListener listener : listeners) {
            listener.handle(event, param);
        }
    }

    public BooleanProperty runningProperty() {
        return running;
    }

    public void setStepMode(boolean isStepMode) {
        this.isStepMode = isStepMode;
    }

    public int getClock() {
        return clock;
    }

    public Sheduler getSheduler() {
        return sheduler;
    }

    public IoDevice getIoDevice() {
        return ioDevice;
    }

    public CPU getCpu(int cpu) {
        return cpus[cpu];
    }

    public ObservableList<PCB> getJobs() {
        return jobs;
    }

    public ProcessQueue getIoWaitingQueue() {
        return ioWaitingQueue;
    }

    public int cpuCount() {
        return nCpu;
    }

    public Thread getCurrentTask() {
        return currentTask;
    }

    public BooleanProperty isDoneProperty() {
        return isDone;
    }

}
