package com.danylevych.mss.model;

public class IoDevice {

    private PCB currentJob = null;

    public void setIdle() {
        currentJob = null;
    }

    public boolean isIdle() {
        return currentJob == null;
    }

    public PCB getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(PCB currentJob) {
        this.currentJob = currentJob;
    }

    public boolean isActive() {
        return !isIdle();
    }
}
