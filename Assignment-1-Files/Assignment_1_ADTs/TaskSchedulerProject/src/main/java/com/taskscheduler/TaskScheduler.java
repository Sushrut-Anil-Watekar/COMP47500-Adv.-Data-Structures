package com.taskscheduler;

import java.util.*;
import java.util.concurrent.*;

public class TaskScheduler {
    private Queue<Task> lowQ = new ConcurrentLinkedQueue<>();
    private Deque<Task> mediumQ = new ConcurrentLinkedDeque<>();
    private Stack<Task> highStack = new Stack<>();

    public void addTask(Task t) {
        switch (t.priority) {
            case LOW:
                lowQ.offer(t);
                break;
            case MEDIUM:
                mediumQ.offerLast(t);
                break;
            case HIGH:
                highStack.push(t);
                break;
        }
    }

    public Task getTask(Priority p) {
        switch (p) {
            case LOW:
                return lowQ.poll();
            case MEDIUM:
                return mediumQ.pollFirst();
            case HIGH:
                if (!highStack.isEmpty()) {
                    return highStack.pop();
                }
                return null;
            default:
                return null;
        }
    }

    public boolean isEmpty() {
        return lowQ.isEmpty() && mediumQ.isEmpty() && highStack.isEmpty();
    }
}
