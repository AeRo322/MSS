package com.danylevych.mss.model;

import static com.danylevych.mss.util.PerfEval.averageWaitTime;
import static com.danylevych.mss.util.PerfEval.cpusUsage;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.danylevych.mss.model.sheduler.Sheduler;
import com.danylevych.mss.model.sheduler.impl.FCFS;

public class ComputerTest {

    @Test
    public void testFCFS() {
        final int nCpu = 2;
        final boolean hasGlobalQueue = true;
        ProcessQueue jobs = new ProcessQueue();

        final int nJobs = 3;
        final List<Integer> cpuBurstsTime = asList(100, 20, 20);

        for (int i = 0; i < nJobs; i++) {
            PCB job = new PCB(i,
                    0,
                    asList(cpuBurstsTime.get(i)),
                    new ArrayList<>());

            jobs.add(job);
        }

        Sheduler sheduler = new FCFS(hasGlobalQueue, nCpu);
        Computer computer = new Computer(jobs, sheduler, nCpu);

        assertEquals(nJobs, sheduler.getReadyQueues().get(0).size());

        computer.run();

        List<ProcessQueue> readyQueues =
                computer.getSheduler().getReadyQueues();

        assertEquals(nCpu, readyQueues.size());

        for (int i = 0; i < readyQueues.size(); i++) {
            assertTrue(readyQueues.get(i).isEmpty());
        }

        assertTrue(computer.getIoWaitingQueue().isEmpty());
        assertEquals(hasGlobalQueue, computer.getSheduler().hasGlobalQueue());

        final double[] cpuUtil = {
            100.0,
            0.0
        };

        assertTrue(Arrays.equals(cpusUsage(computer), cpuUtil));
        assertEquals(140, computer.getClock());

        final List<Integer> expectedWaits = asList(0, 100, 120);
        List<Integer> actualWaits = new ArrayList<>();
        computer.getJobs().forEach(e -> actualWaits.add(e.getWaitingTime()));

        assertEquals(expectedWaits, actualWaits);

        final double avWaitTime = (0.0 + 100.0 + 120.0) / 3.0;
        assertEquals(avWaitTime, averageWaitTime(computer), 0.01);
    }

}
