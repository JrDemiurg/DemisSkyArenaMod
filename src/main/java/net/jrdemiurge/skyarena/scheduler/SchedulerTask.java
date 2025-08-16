package net.jrdemiurge.skyarena.scheduler;

public class SchedulerTask {
    private int ticksRemaining;
    private final Runnable task;

    public SchedulerTask(int delay, Runnable task) {
        this.ticksRemaining = delay;
        this.task = task;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void setTicksRemaining(int ticksRemaining) {
        this.ticksRemaining = ticksRemaining;
    }

    public Runnable getTask() {
        return task;
    }
}
