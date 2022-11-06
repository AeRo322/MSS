package com.danylevych.mss.model.sheduler;

import java.util.List;

import com.danylevych.mss.model.PCB;
import com.danylevych.mss.model.ProcessQueue;

public interface Sheduler {

    int addJob(PCB job);

    PCB nextJob(int cpu);

    boolean hasNextJob(int cpu);

    List<ProcessQueue> getReadyQueues();

    ProcessQueue getReadyQueue(int cpu);

    ProcessQueue getGlobalQueue();

    boolean hasGlobalQueue();

    int getLastAddedJobPos();

}
