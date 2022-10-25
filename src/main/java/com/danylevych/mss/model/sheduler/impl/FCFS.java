package com.danylevych.mss.model.sheduler.impl;

import static com.danylevych.mss.util.ProcessQueueUtils.getSmallestQueue;

import java.util.ArrayList;
import java.util.List;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.ProcessQueue;
import com.danylevych.mss.model.sheduler.Sheduler;

public class FCFS implements Sheduler {

    private List<ProcessQueue> readyQueues = new ArrayList<>();
    private ProcessQueue globalQueue = new ProcessQueue();
    private final boolean hasGlobalQueue;

    public FCFS(boolean hasGlobalQueue, int nCpu) {
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

    /**
     * @return queue ID
     */
    @Override
    public int addJob(PCB job) {
        if (hasGlobalQueue) {
            globalQueue.add(job);
            return 0;
        }

        return addToLeastLoadedQueue(job);
    }

    @Override
    public PCB nextJob(int cpu) {
        ProcessQueue queue = readyQueues.get(cpu);

        if (queue.isEmpty()) {
            return null;
        }

        return queue.removeFirst();
    }

    @Override
    public boolean hasNextJob(int cpu) {
        return nextJob(cpu) != null;
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

    private int addToLeastLoadedQueue(PCB job) {
        ProcessQueue leastLoadedQueue = getSmallestQueue(readyQueues);
        leastLoadedQueue.add(job);
        return readyQueues.indexOf(leastLoadedQueue);
    }

}
