import java.util.*;
import java.util.concurrent.*;

public class TaskSchedulerExperiment {

    // Priority levels for tasks
    enum Priority {
        LOW, MEDIUM, HIGH
    }

    // Task class representing a unit of work, with timing metrics and shared lists for data collection
    static class Task {
        // Shared, synchronized lists for all tasks
        private static final List<Long> waitTimes =
            Collections.synchronizedList(new ArrayList<Long>());
        private static final List<Long> turnaroundTimes =
            Collections.synchronizedList(new ArrayList<Long>());

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

        // Simulate task processing and record timing metrics
        public void process() {
            this.startTime = System.currentTimeMillis();

            try {
                Thread.sleep(50);  // simulate some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            this.finishTime = System.currentTimeMillis();

            long waitTime = startTime - creationTime;
            long turnaroundTime = finishTime - creationTime;

            // Record data in shared lists
            waitTimes.add(waitTime);
            turnaroundTimes.add(turnaroundTime);

            // Print a log line
            System.out.println("Processed Task " + id + " with " + priority + " priority | "
                    + "Wait Time: " + waitTime + "ms, "
                    + "Turnaround Time: " + turnaroundTime + "ms, "
                    + "Processed by: " + Thread.currentThread().getName());
        }

        // Getters for the static lists so we can compute stats later
        public static List<Long> getWaitTimes() {
            return waitTimes;
        }
        public static List<Long> getTurnaroundTimes() {
            return turnaroundTimes;
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
                    mediumPriorityDeque.offerLast(task);
                    break;
                case HIGH:
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
                    return mediumPriorityDeque.pollFirst();
                case HIGH:
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
            return lowPriorityQueue.isEmpty()
                && mediumPriorityDeque.isEmpty()
                && highPriorityStack.isEmpty();
        }
    }

    // Worker thread that polls tasks from a specific ADT (based on priority).
    static class Worker implements Runnable {
        private final TaskScheduler scheduler;
        private final Priority priority;

        public Worker(TaskScheduler scheduler, Priority priority) {
            this.scheduler = scheduler;
            this.priority = priority;
        }

        @Override
        public void run() {
            while (!scheduler.isEmpty()) {
                Task task = scheduler.getTask(priority);
                if (task != null) {
                    task.process();
                } else {
                    // Pause if no task is available for this priority.
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        // Start timer for throughput
        long overallStart = System.currentTimeMillis();

        TaskScheduler scheduler = new TaskScheduler();
        Random random = new Random();

        // Generate 100 tasks with random priorities
        for (int i = 1; i <= 100; i++) {
            Priority priority;
            int rand = random.nextInt(3); // 0, 1, or 2
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

        // Create a fixed thread pool with three workers (one for each priority type)
        ExecutorService executor = Executors.newFixedThreadPool(3);
        executor.execute(new Worker(scheduler, Priority.HIGH));
        executor.execute(new Worker(scheduler, Priority.MEDIUM));
        executor.execute(new Worker(scheduler, Priority.LOW));

        // Shutdown after processing
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("All tasks have been processed.");

        // Stop timer for throughput
        long overallFinish = System.currentTimeMillis();
        long totalElapsed = overallFinish - overallStart;

        // Compute and print stats
        computeAndPrintStatistics(totalElapsed);
    }

    // Helper to compute and print all stats
    private static void computeAndPrintStatistics(long totalElapsedMillis) {
        List<Long> waitTimes = Task.getWaitTimes();
        List<Long> turnaroundTimes = Task.getTurnaroundTimes();

        if (waitTimes.isEmpty()) {
            System.out.println("No tasks were processed.");
            return;
        }

        int numberOfTasks = waitTimes.size();
        double totalSeconds = totalElapsedMillis / 1000.0;
        double throughput = numberOfTasks / totalSeconds;

        // Wait times
        long minWait = Collections.min(waitTimes);
        long maxWait = Collections.max(waitTimes);
        double avgWait = average(waitTimes);
        double stdDevWait = standardDeviation(waitTimes, avgWait);

        // Turnaround times
        long minTAT = Collections.min(turnaroundTimes);
        long maxTAT = Collections.max(turnaroundTimes);
        double avgTAT = average(turnaroundTimes);
        double stdDevTAT = standardDeviation(turnaroundTimes, avgTAT);

        System.out.println("\n========= STATISTICS =========");
        System.out.println("Total Tasks Processed: " + numberOfTasks);
        System.out.printf("Total Elapsed Time: %.2f seconds%n", totalSeconds);
        System.out.printf("Throughput: %.2f tasks/second%n", throughput);

        System.out.println("\nWait Time (ms):");
        System.out.println("  Min = " + minWait);
        System.out.println("  Max = " + maxWait);
        System.out.printf ("  Avg = %.2f%n", avgWait);
        System.out.printf ("  Std Dev = %.2f%n", stdDevWait);

        System.out.println("\nTurnaround Time (ms):");
        System.out.println("  Min = " + minTAT);
        System.out.println("  Max = " + maxTAT);
        System.out.printf ("  Avg = %.2f%n", avgTAT);
        System.out.printf ("  Std Dev = %.2f%n", stdDevTAT);

        System.out.println("=============================\n");
    }

    // Compute average
    private static double average(List<Long> values) {
        long sum = 0;
        for (long v : values) {
            sum += v;
        }
        return (double) sum / values.size();
    }

    // Compute standard deviation
    private static double standardDeviation(List<Long> values, double mean) {
        double temp = 0;
        for (long v : values) {
            double diff = v - mean;
            temp += diff * diff;
        }
        return Math.sqrt(temp / values.size());
    }
}
