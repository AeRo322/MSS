package com.danylevych.mss.model.sheduler.impl;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.sheduler.BaseSheduler;

public class FCFS extends BaseSheduler {

    public FCFS(boolean hasGlobalQueue, int nCpu) {
        super(hasGlobalQueue, nCpu);
    }

    /**
     * @return queue ID
     */
    @Override
    public int addJob(PCB job) {
        int queue = 0;

        if (hasGlobalQueue) {
            globalQueue.add(job);
        } else {
            queue = addToLeastLoadedQueue(job);
        }

        lastAddedJobPos = getReadyQueue(queue).size() - 1;

        return queue;
    }

}
