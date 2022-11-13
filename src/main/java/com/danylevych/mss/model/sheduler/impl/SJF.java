package com.danylevych.mss.model.sheduler.impl;

import java.util.Comparator;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.ProcessQueue;
import com.danylevych.mss.model.sheduler.BaseSheduler;

public class SJF extends BaseSheduler {

    public SJF(boolean hasGlobalQueue, int nCpu) {
        super(hasGlobalQueue, nCpu);
    }

    @Override
    public int addJob(PCB job) {
        int queue = 0;

        if (hasGlobalQueue) {
            globalQueue.add(job);
        } else {
            queue = addToLeastLoadedQueue(job);
        }

        ProcessQueue readyQueue = getReadyQueue(queue);
        readyQueue.sort(Comparator.comparingInt(PCB::getArrivalTime));

        lastAddedJobPos = readyQueue.indexOf(job);

        return queue;
    }

}
