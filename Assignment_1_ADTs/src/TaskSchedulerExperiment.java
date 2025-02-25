

import java.util.*;
import java.util.concurrent.*;

public class TaskSchedulerExperiment {

    // Priority levels for tasks
    enum Priority {
        LOW, MEDIUM, HIGH
    }

    // Task class representing a unit of work, now with timing metrics.
    static class Task {
        int id;
        Priority priority;
        long creationTime;
        long startTime;
        long finishTime;

        public Task(int id, Priority priority) {
            this.id = id;
            this.priority = priority;
            this.creationTime = System.currentTimeMillis();
        }

        // Simulate task processing and record timing metrics.
        public void process() {
            // Record the start time (when processing begins)
            this.startTime = System.currentTimeMillis();
            
            try {
                // Simulate processing work (e.g., 50ms delay)
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Record the finish time (when processing completes)
            this.finishTime = System.currentTimeMillis();
            
            // Calculate wait time and turnaround time
            long waitTime = startTime - creationTime;
            long turnaroundTime = finishTime - creationTime;
            
            // Log detailed timing information
            System.out.println("Processed Task " + id + " with " + priority + " priority | " +
                               "Wait Time: " + waitTime + "ms, " +
                               "Turnaround Time: " + turnaroundTime + "ms, " +
                               "Processed by: " + Thread.currentThread().getName());
        }
    }

    // Scheduler managing three ADT structures.
    static class TaskScheduler {
    	// Low-priority tasks: FIFO queue
        private Queue<Task> lowPriorityQueue = new ConcurrentLinkedQueue<>();
        // High-priority tasks: Stack (synchronized by default)
        private Stack<Task> highPriorityStack = new Stack<>();
        // Medium-priority tasks: Deque
        private Deque<Task> mediumPriorityDeque = new ConcurrentLinkedDeque<>();

        // Add a task to the appropriate ADT based on its priority.
        public void addTask(Task task) {
            switch (task.priority) {
                case LOW:
                    lowPriorityQueue.offer(task);
                    break;
                case MEDIUM:
                    // Insert at the rear of the deque.
                    mediumPriorityDeque.offer(task);
                    break;
                case HIGH:
                    // Push onto the stack.
                    highPriorityStack.push(task);
                    break;
            }
        }

        // Retrieve a task from the appropriate ADT.
        public Task getTask(Priority priority) {
            switch (priority) {
                case LOW:
                    return lowPriorityQueue.poll();
                case MEDIUM:
                    // Retrieve from the front of the deque.
                    return mediumPriorityDeque.pollFirst();
                case HIGH:
                    // Pop from the stack.
                    if (!highPriorityStack.isEmpty()) {
                        return highPriorityStack.pop();
                    }
                    return null;
                default:
                    return null;
            }
        }

        // Check if all ADTs are empty.
        public boolean isEmpty() {
            return lowPriorityQueue.isEmpty() && mediumPriorityDeque.isEmpty() && highPriorityStack.isEmpty();
        }
    }

    // Worker thread that polls tasks from a specific ADT (based on priority).
    static class Worker implements Runnable {
        //add code here
    }

    public static void main(String[] args) {
        TaskScheduler scheduler = new TaskScheduler();
        Random random = new Random();

        // Generate 100 tasks with random priorities.
        for (int i = 1; i <= 100; i++) {
            Priority priority;
            int rand = random.nextInt(3); // Generates 0, 1, or 2.
            if (rand == 0) {
                priority = Priority.LOW;
            } else if (rand == 1) {
                priority = Priority.MEDIUM;
            } else {
                priority = Priority.HIGH;
            }
            Task task = new Task(i, priority);
            scheduler.addTask(task);
        }

        // Create a fixed thread pool with three workers (one for each priority type).
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(new Worker(scheduler, Priority.HIGH));
        executor.execute(new Worker(scheduler, Priority.MEDIUM));
        executor.execute(new Worker(scheduler, Priority.LOW));

        // Shutdown the executor after processing all tasks.
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("All tasks have been processed.");
    }
}

