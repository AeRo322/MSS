package com.danylevych.mss.model.sheduler;

import static com.danylevych.mss.util.ProcessQueueUtils.getSmallestQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.ProcessQueue;

public abstract class BaseSheduler implements Sheduler {

    protected final List<ProcessQueue> readyQueues = new ArrayList<>();
    protected final ProcessQueue globalQueue = new ProcessQueue();
    protected final boolean hasGlobalQueue;
    protected int lastAddedJobPos;

    protected BaseSheduler(boolean hasGlobalQueue, int nCpu) {
        this.hasGlobalQueue = hasGlobalQueue;

        if (hasGlobalQueue) {
            for (int i = 0; i < nCpu; i++) {
                readyQueues.add(globalQueue);
            }
        } else {
            for (int i = 0; i < nCpu; i++) {
                readyQueues.add(new ProcessQueue());
            }
        }
    }

    @Override
    public PCB nextJob(int cpu) {
        if (!hasNextJob(cpu)) {
            throw new NoSuchElementException();
        }

        if (lastAddedJobPos > 0) {
            lastAddedJobPos--;
        }
        
        return readyQueues.get(cpu).removeFirst();
    }

    @Override
    public boolean hasNextJob(int cpu) {
        return !readyQueues.get(cpu).isEmpty();
    }

    @Override
    public List<ProcessQueue> getReadyQueues() {
        return readyQueues;
    }

    @Override
    public ProcessQueue getReadyQueue(int cpu) {
        return readyQueues.get(cpu);
    }

    @Override
    public ProcessQueue getGlobalQueue() {
        return globalQueue;
    }

    @Override
    public boolean hasGlobalQueue() {
        return this.hasGlobalQueue;
    }

    @Override
    public int getLastAddedJobPos() {
        return lastAddedJobPos;
    }

    protected int addToLeastLoadedQueue(PCB job) {
        ProcessQueue leastLoadedQueue = getSmallestQueue(readyQueues);
        leastLoadedQueue.add(job);
        return readyQueues.indexOf(leastLoadedQueue);
    }
}
