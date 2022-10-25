package com.danylevych.mss.util;

import com.danylevych.mss.model.Computer;
import com.danylevych.mss.model.PCB;

public class PerfEval {

    private PerfEval() {

    }

    public static int responseTime(int firstRunTime, int arrivalTime) {
        return firstRunTime - arrivalTime;
    }

    public static int tat(int finishTime, int arrivalTime) {
        return finishTime - arrivalTime;
    }

    public static double averageWaitTime(Computer computer) {
        double waitTime = 0.0;

        for (PCB job : computer.getJobs()) {
            waitTime += job.getWaitingTime();
        }

        return waitTime / computer.getJobs().size();
    }

    public static double[] cpusUsage(Computer computer) {
        final int cpuCount = computer.cpuCount();
        final int clock = computer.getClock();
        double[] cpusUsage = new double[cpuCount];

        for (int i = 0; i < cpuCount; i++) {
            cpusUsage[i] = 100.0 * computer.getCpu(i).getRunTime() / clock;
        }

        return cpusUsage;
    }

    public static int[] tat(Computer computer) {
        final int cpuCount = computer.cpuCount();
        final int clock = computer.getClock();
        int[] tats = new int[cpuCount];

        for (int i = 0; i < cpuCount; i++) {
            tats[i] = computer.getCpu(i).getRunTime() / clock;
        }

        return tats;
    }
}