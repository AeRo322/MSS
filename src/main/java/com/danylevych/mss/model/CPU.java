package com.danylevych.mss.model;

public class CPU {

    private PCB currentJob;
    private int runTime;

    public boolean incrementRuntime() {
        final boolean isActive = isActive();
        if (isActive) {
            runTime++;
        }
        return isActive;
    }

    public PCB setIdle() {
        PCB oldJob = this.currentJob;
        this.currentJob = null;
        return oldJob;
    }

    public boolean isIdle() {
        return currentJob == null;
    }

    public boolean isActive() {
        return !isIdle();
    }

    public PCB getCurrentJob() {
        return currentJob;
    }

    public void assignJob(PCB job) {
        this.currentJob = job;
    }

    public int getRunTime() {
        return runTime;
    }

    public void setRunTime(int runTime) {
        this.runTime = runTime;
    }
}
