package com.taskscheduler;

import static org.junit.Assert.*;
import org.junit.Test;

public class TaskSchedulerTest {

    @Test
    public void testTaskInsertionAndRetrieval() {
        // Create a new scheduler instance
        TaskScheduler scheduler = new TaskScheduler();

        // Create test tasks for each priority level
        Task lowTask = new Task(1, Priority.LOW);
        Task mediumTask = new Task(2, Priority.MEDIUM);
        Task highTask = new Task(3, Priority.HIGH);

        // Add tasks to the scheduler
        scheduler.addTask(lowTask);
        scheduler.addTask(mediumTask);
        scheduler.addTask(highTask);

        // Verify tasks are retrieved in correct order
        assertEquals(highTask, scheduler.getTask(Priority.HIGH)); // Stack (LIFO)
        assertEquals(mediumTask, scheduler.getTask(Priority.MEDIUM)); // Deque (FIFO)
        assertEquals(lowTask, scheduler.getTask(Priority.LOW)); // Queue (FIFO)
    }

    @Test
    public void testEmptyQueueBehavior() {
        // Create an empty scheduler
        TaskScheduler scheduler = new TaskScheduler();

        // Ensure retrieving from an empty queue returns null
        assertNull(scheduler.getTask(Priority.LOW));
        assertNull(scheduler.getTask(Priority.MEDIUM));
        assertNull(scheduler.getTask(Priority.HIGH));
    }
}
