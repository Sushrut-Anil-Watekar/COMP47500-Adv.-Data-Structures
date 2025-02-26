package com.taskscheduler;

import static org.junit.Assert.*;
import org.junit.Test;

public class TaskTest {
    @Test
    public void testTaskProcessing() throws InterruptedException {
        Task task = new Task(1, Priority.HIGH);
        Thread.sleep(50); // Simulating delay
        task.process();
        
        assertTrue(task.startTime >= task.creationTime);
        assertTrue(task.finishTime >= task.startTime);

        // Allow ±20ms deviation in processing time
        assertEquals(50, task.finishTime - task.startTime, 20);  
    }
}
