package com.taskscheduler;

import java.util.*;
import java.util.concurrent.*;

public class Task {
    private static final List<Long> allWaitTimes =
            Collections.synchronizedList(new ArrayList<>());
    private static final List<Long> allTurnaroundTimes =
            Collections.synchronizedList(new ArrayList<>());

    int id;
    Priority priority;
    long creationTime;
    long startTime;
    long finishTime;

    public Task(int i, Priority prio) {
        this.id = i;
        this.priority = prio;
        this.creationTime = System.currentTimeMillis();
    }

    public void process() {
        startTime = System.currentTimeMillis();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        finishTime = System.currentTimeMillis();

        long wait = startTime - creationTime;
        long turnaround = finishTime - creationTime;

        allWaitTimes.add(wait);
        allTurnaroundTimes.add(turnaround);

        System.out.println("Processed Task " + id + " (" + priority + ") | " +
                           "Wait: " + wait + "ms, " +
                           "Turnaround: " + turnaround + "ms, " +
                           "Thread: " + Thread.currentThread().getName());
    }

    public static List<Long> getAllWaitTimes() { return allWaitTimes; }
    public static List<Long> getAllTurnaroundTimes() { return allTurnaroundTimes; }

    public static void resetMetrics() {
        allWaitTimes.clear();
        allTurnaroundTimes.clear();
    }
}
