package com.taskscheduler;

import static org.junit.Assert.*;
import org.junit.Test;

public class WorkerTest {

    @Test
    public void testWorkerProcessing() throws InterruptedException {
        // Create a task scheduler
        TaskScheduler scheduler = new TaskScheduler();

        // Add multiple HIGH priority tasks to ensure they are processed
        for (int i = 1; i <= 50; i++) {
            scheduler.addTask(new Task(i, Priority.HIGH));
        }

        // Create and start worker threads
        Thread worker1 = new Thread(new Worker(scheduler, Priority.HIGH));
        Thread worker2 = new Thread(new Worker(scheduler, Priority.HIGH));

        worker1.start();
        worker2.start();

        // Wait for both workers to finish
        worker1.join();
        worker2.join();

        // Ensure that all tasks have been processed
        assertTrue(scheduler.isEmpty());
    }
}
