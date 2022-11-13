package com.danylevych.mss.model;

import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static javafx.collections.FXCollections.observableArrayList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.danylevych.mss.App;
import com.danylevych.mss.model.event.Event;
import com.danylevych.mss.model.event.EventListener;
import com.danylevych.mss.model.sheduler.Sheduler;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;

public class Computer {

    private final ProcessQueue ioWaitingQueue = new ProcessQueue();
    private final IoDevice ioDevice = new IoDevice();

    private final Sheduler sheduler;
    private final int jobsToDo;
    private final int quantum;

    private final CPU[] cpus;
    private final int nCpu;

    private final BooleanProperty running = new SimpleBooleanProperty(false);
    private final BooleanProperty isDone = new SimpleBooleanProperty(false);

    private final ExecutorService executorService = newCachedThreadPool();
    private final List<EventListener> listeners = new ArrayList<>();
    private final ObservableList<PCB> jobs;

    private int nListenersDone;
    private boolean isStepMode;
    private int nextJobIndex;
    private int jobsDone;
    private int clock;

    public Computer(ProcessQueue jobs, Sheduler sheduler, int nCpu,
            int quantum) {
        validateParams(jobs, sheduler, nCpu);

        this.jobs = observableArrayList(jobs);
        this.jobs.sort(Comparator.comparingInt(PCB::getArrivalTime));

        this.jobsToDo = jobs.size();
        this.sheduler = sheduler;
        this.quantum = quantum;

        this.nCpu = nCpu;
        this.cpus = new CPU[nCpu];
        for (int i = 0; i < nCpu; i++) {
            cpus[i] = new CPU();
        }

        App.addOnExit(executorService::shutdown);
    }

    public Computer(ProcessQueue jobs, Sheduler sheduler, int nCpu) {
        this(jobs, sheduler, nCpu, -1);
    }

    private static void validateParams(
            ProcessQueue jobs,
            Sheduler sheduler,
            int nCpu) {

        requireNonNull(sheduler);

        if (jobs == null || jobs.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (nCpu <= 0) {
            throw new IllegalArgumentException();
        }
    }

    public synchronized void run() {
        if (running.get()) {
            throw new IllegalStateException("Already running");
        }

        if (listeners.isEmpty()) {
            work();
        } else {
            executorService.execute(this::work);
        }
    }

    private void work() {
        running.set(true);

        while (!isDone.get()) {

            addJobs();
            handleInterrupts();

            assignCpuJobs();
            assignIoJob();

            execCpuJobs();
            execIoJob();

            incrementClock();

            isDone.set(jobsToDo == jobsDone);

            if (isStepMode) {
                break;
            }
        }

        running.set(false);
    }

    private void addJobs() {
        for (int i = nextJobIndex; i < jobsToDo; i++) {
            PCB job = jobs.get(i);
            if (job.getArrivalTime() == clock) {
                nextJobIndex++;
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
        return quantum > 0;
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

    private void assignIoJob() {
        if (!ioWaitingQueue.isEmpty() && ioDevice.isIdle()) {
            ioDevice.setCurrentJob(ioWaitingQueue.removeFirst());
            fireEvent(Event.JOB_ASSIGNED, -1);
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
            notify(event, param);
            join();
        }
    }

    private void notify(Event event, int param) {
        for (EventListener listener : listeners) {
            executorService.execute(() -> listener.handle(event, param));
        }
    }

    private synchronized void join() {
        while (listenersWorking()) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException(e);
            }
        }

        nListenersDone = 0;
    }

    private boolean listenersWorking() {
        return nListenersDone != listeners.size();
    }

    public void addEventListener(EventListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void listenerJobDone() {
        nListenersDone++;
        notifyAll();
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

    public BooleanProperty isDoneProperty() {
        return isDone;
    }

}
