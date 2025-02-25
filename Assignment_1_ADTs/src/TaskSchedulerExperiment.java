import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class TaskSchedulerExperiment {

    // We'll define three levels of priority for tasks.
    enum Priority {
        LOW, MEDIUM, HIGH
    }

    // ----------------------------------------------------------------------------------
    // A Task represents a piece of work. It knows when it was created, started, and finished.
    // We'll track wait times & turnaround times in two static lists.
    // ----------------------------------------------------------------------------------
    static class Task {

        // We store aggregate metrics for all tasks in these lists.
        private static final List<Long> allWaitTimes =
                Collections.synchronizedList(new ArrayList<>());
        private static final List<Long> allTurnaroundTimes =
                Collections.synchronizedList(new ArrayList<>());

        // Individual fields for each task.
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

        // Called when a Worker picks up this Task.
        // It simulates the actual "work" by sleeping 50ms, then calculates wait & turnaround.
        public void process() {
            startTime = System.currentTimeMillis();
            try {
                Thread.sleep(50); // Doing "work"
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finishTime = System.currentTimeMillis();

            // Compute wait and turnaround times for this task.
            long wait = startTime - creationTime;       // time spent waiting
            long turnaround = finishTime - creationTime; // total time from creation to finish

            // Stash them into the shared lists.
            allWaitTimes.add(wait);
            allTurnaroundTimes.add(turnaround);

            // For demonstration, log each processed task to the console:
            System.out.println("Processed Task " + id + " (" + priority + ") | " +
                               "Wait: " + wait + "ms, Turnaround: " + turnaround + "ms, " +
                               "Thread: " + Thread.currentThread().getName());
        }

        // Accessors for the static lists (for analysis).
        public static List<Long> getAllWaitTimes() { return allWaitTimes; }
        public static List<Long> getAllTurnaroundTimes() { return allTurnaroundTimes; }
    }

    // ----------------------------------------------------------------------------------
    // TaskScheduler holds three separate ADTs for tasks of different priorities:
    //   - Stack for HIGH
    //   - Queue for LOW
    //   - Deque for MEDIUM
    // ----------------------------------------------------------------------------------
    static class TaskScheduler {

        private Queue<Task> lowQ = new ConcurrentLinkedQueue<>();
        private Deque<Task> mediumQ = new ConcurrentLinkedDeque<>();
        private Stack<Task> highStack = new Stack<>();

        // Based on the task's priority, store it in the relevant data structure.
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

        // Worker threads call getTask with a specific priority to retrieve their next task.
        public Task getTask(Priority prio) {
            switch (prio) {
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

        // If all three data structures are empty, scheduling is done.
        public boolean isEmpty() {
            return lowQ.isEmpty() && mediumQ.isEmpty() && highStack.isEmpty();
        }
    }

    // ----------------------------------------------------------------------------------
    // Worker is a Runnable that continuously retrieves tasks from one ADT (based on Priority).
    // It calls task.process() for each retrieved task.
    // ----------------------------------------------------------------------------------
    static class Worker implements Runnable {

        private final TaskScheduler scheduler;
        private final Priority prio;

        public Worker(TaskScheduler sched, Priority p) {
            this.scheduler = sched;
            this.prio = p;
        }

        @Override
        public void run() {
            while (!scheduler.isEmpty()) {
                Task t = scheduler.getTask(prio);
                if (t != null) {
                    t.process();
                } else {
                    // If no task is currently available for this priority, nap briefly.
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    // ----------------------------------------------------------------------------------
    // MAIN: we create 100 tasks randomly assigned to LOW, MEDIUM, or HIGH,
    // start three worker threads, then compute stats and append them to a CSV file.
    // ----------------------------------------------------------------------------------
    public static void main(String[] args) {

        long startTimeMs = System.currentTimeMillis(); // We'll measure total runtime to get throughput.

        // Make a new scheduler with three data structures inside.
        TaskScheduler scheduler = new TaskScheduler();

        // Create 100 tasks at random priorities.
        Random rand = new Random();
        for (int i = 1; i <= 100; i++) {
            int pick = rand.nextInt(3);  // 0, 1, or 2
            Priority p;
            if (pick == 0) p = Priority.LOW;
            else if (pick == 1) p = Priority.MEDIUM;
            else p = Priority.HIGH;

            Task newTask = new Task(i, p);
            scheduler.addTask(newTask);
        }

        // We'll spawn exactly 3 threads, each dedicated to a different priority.
        ExecutorService pool = Executors.newFixedThreadPool(3);
        pool.execute(new Worker(scheduler, Priority.HIGH));
        pool.execute(new Worker(scheduler, Priority.MEDIUM));
        pool.execute(new Worker(scheduler, Priority.LOW));

        // Wait for all tasks to finish or time out in 10s, then shut down the executor.
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }

        System.out.println("All tasks have been processed.");

        long finishTimeMs = System.currentTimeMillis();
        long totalElapsedMs = finishTimeMs - startTimeMs;

        // We'll gather the aggregated metrics and print them out:
        computeAndPrintStatistics(totalElapsedMs);
    }

    // ----------------------------------------------------------------------------------
    // This method does all the stats calculations (min, max, avg, std dev, throughput),
    // logs them to the console, and appends them to a CSV file "runs.csv".
    // ----------------------------------------------------------------------------------
    private static void computeAndPrintStatistics(long elapsedTimeMs) {

        List<Long> waitData = Task.getAllWaitTimes();
        List<Long> turnaroundData = Task.getAllTurnaroundTimes();

        if (waitData.isEmpty()) {
            System.out.println("No tasks processed. Something's off!");
            return;
        }

        int totalTasks = waitData.size();
        double totalSecs = elapsedTimeMs / 1000.0;
        double tasksPerSec = totalTasks / totalSecs;

        // Wait time stats
        long minW = Collections.min(waitData);
        long maxW = Collections.max(waitData);
        double avgW = computeAverage(waitData);
        double stdW = computeStdDev(waitData, avgW);

        // Turnaround time stats
        long minT = Collections.min(turnaroundData);
        long maxT = Collections.max(turnaroundData);
        double avgT = computeAverage(turnaroundData);
        double stdT = computeStdDev(turnaroundData, avgT);

        System.out.println("\n========== EXPERIMENT RESULTS ==========");
        System.out.println("Tasks: " + totalTasks);
        System.out.printf("Total Elapsed: %.2f s\n", totalSecs);
        System.out.printf("Throughput: %.2f tasks/s\n\n", tasksPerSec);

        System.out.println("Wait Time (ms): ");
        System.out.println("  Min = " + minW);
        System.out.println("  Max = " + maxW);
        System.out.printf ("  Avg = %.2f\n", avgW);
        System.out.printf ("  Std = %.2f\n", stdW);

        System.out.println("\nTurnaround Time (ms): ");
        System.out.println("  Min = " + minT);
        System.out.println("  Max = " + maxT);
        System.out.printf ("  Avg = %.2f\n", avgT);
        System.out.printf ("  Std = %.2f\n", stdT);
        System.out.println("========================================\n");

        // Append this data as a new row to "runs.csv".
        appendCsvRow("runs.csv", System.currentTimeMillis(),
                     totalSecs, tasksPerSec,
                     minW, maxW, avgW, stdW,
                     minT, maxT, avgT, stdT);
    }

    // A quick average calculator.
    private static double computeAverage(List<Long> vals) {
        long sum = 0;
        for (long v : vals) {
            sum += v;
        }
        return (double) sum / vals.size();
    }

    // A quick standard deviation calculator.
    private static double computeStdDev(List<Long> vals, double mean) {
        double sumOfSquares = 0.0;
        for (long v : vals) {
            double diff = v - mean;
            sumOfSquares += diff * diff;
        }
        return Math.sqrt(sumOfSquares / vals.size());
    }

    // ----------------------------------------------------------------------------------
    // We'll store each run's summarized stats in a single line of CSV in "runs.csv".
    // If the file doesn't exist, we create it and write a header.
    // If it does exist, we append just one line for this new run.
    // ----------------------------------------------------------------------------------
    private static void appendCsvRow(
        String fileName, long timestamp,
        double totalTime, double throughputVal,
        long minWait, long maxWait, double avgWait, double stdWait,
        long minTAT, long maxTAT, double avgTAT, double stdTAT
    ) {
        boolean fileExists = Files.exists(Paths.get(fileName));

        try (FileWriter fw = new FileWriter(fileName, true)) {
            if (!fileExists) {
                fw.write("Timestamp,TotalTime(s),Throughput,MinWait,MaxWait,AvgWait,StdWait,MinTAT,MaxTAT,AvgTAT,StdTAT\n");
            }

            fw.write(String.format(
                "%d,%.2f,%.2f,%d,%d,%.2f,%.2f,%d,%d,%.2f,%.2f\n",
                timestamp,
                totalTime,
                throughputVal,
                minWait,
                maxWait,
                avgWait,
                stdWait,
                minTAT,
                maxTAT,
                avgTAT,
                stdTAT
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
