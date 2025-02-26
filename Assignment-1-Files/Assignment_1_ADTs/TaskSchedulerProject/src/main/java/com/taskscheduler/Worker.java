package com.taskscheduler;

public class Worker implements Runnable {
    private final TaskScheduler scheduler;
    private final Priority prio;

    public Worker(TaskScheduler s, Priority p) {
        this.scheduler = s;
        this.prio = p;
    }

    @Override
    public void run() {
        while (!scheduler.isEmpty()) {
            Task task = scheduler.getTask(prio);
            if (task != null) {
                task.process();
            } else {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
