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
    // We'll track wait times & turnaround times in two static lists (shared by all tasks).
    // ----------------------------------------------------------------------------------
    static class Task {

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

            // Store metrics for post-run analysis.
            allWaitTimes.add(wait);
            allTurnaroundTimes.add(turnaround);

            // Optional: log each processed task to the console.
            System.out.println("Processed Task " + id + " (" + priority + ") | " +
                               "Wait: " + wait + "ms, " +
                               "Turnaround: " + turnaround + "ms, " +
                               "Thread: " + Thread.currentThread().getName());
        }

        // Accessors for the static lists.
        public static List<Long> getAllWaitTimes() {
            return allWaitTimes;
        }
        public static List<Long> getAllTurnaroundTimes() {
            return allTurnaroundTimes;
        }

        // Clear old data so each run is independent.
        public static void resetMetrics() {
            allWaitTimes.clear();
            allTurnaroundTimes.clear();
        }
    }

    // ----------------------------------------------------------------------------------
    // TaskScheduler: three separate ADTs for different priorities: Stack, Queue, Deque.
    // ----------------------------------------------------------------------------------
    static class TaskScheduler {

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

    // ----------------------------------------------------------------------------------
    // Worker is a Runnable that continuously retrieves tasks for one specific Priority.
    // ----------------------------------------------------------------------------------
    static class Worker implements Runnable {
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

    // ----------------------------------------------------------------------------------
    // Main runs the entire experiment 50 times in a row. Each run:
    // 1) Resets metrics
    // 2) Creates tasks
    // 3) Processes them
    // 4) Prints stats and appends them to "runs.csv"
    // ----------------------------------------------------------------------------------
    public static void main(String[] args) {
        // Number of times we want to run the experiment automatically.
        int totalRuns = 50;

        for (int runNumber = 1; runNumber <= totalRuns; runNumber++) {
            System.out.println("\n=============================");
            System.out.println("Starting run " + runNumber + " of " + totalRuns);
            System.out.println("=============================");

            // Reset static metrics so each run is fresh.
            Task.resetMetrics();

            // Execute one experiment
            runSingleExperiment();
        }

        System.out.println("\nAll " + totalRuns + " runs have completed.");
    }

    // ----------------------------------------------------------------------------------
    // This method performs a single experiment:
    //    - Creates 100 tasks
    //    - Runs 3 worker threads
    //    - Waits until they're done
    //    - Prints stats + writes CSV row
    // ----------------------------------------------------------------------------------
    private static void runSingleExperiment() {
        long startMillis = System.currentTimeMillis();

        TaskScheduler scheduler = new TaskScheduler();
        Random rand = new Random();

        // Create 100 tasks, random priority, and add them to the scheduler
        for (int i = 1; i <= 100; i++) {
            int pick = rand.nextInt(3); // 0,1,2
            Priority p = (pick == 0) ? Priority.LOW
                                     : (pick == 1) ? Priority.MEDIUM : Priority.HIGH;

            Task newTask = new Task(i, p);
            scheduler.addTask(newTask);
        }

        // Create three threads (one for each priority)
        ExecutorService pool = Executors.newFixedThreadPool(3);
        pool.execute(new Worker(scheduler, Priority.HIGH));
        pool.execute(new Worker(scheduler, Priority.MEDIUM));
        pool.execute(new Worker(scheduler, Priority.LOW));

        // Wait for tasks to complete or time out
        pool.shutdown();
        try {
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException e) {
            pool.shutdownNow();
        }

        long finishMillis = System.currentTimeMillis();
        long elapsed = finishMillis - startMillis;

        // Print / record stats
        computeAndRecordStats(elapsed);
    }

    // ----------------------------------------------------------------------------------
    // This method collects wait/turnaround times, prints them, and appends one line to "runs.csv"
    // ----------------------------------------------------------------------------------
    private static void computeAndRecordStats(long elapsedMs) {

        List<Long> waits = Task.getAllWaitTimes();
        List<Long> tats = Task.getAllTurnaroundTimes();

        if (waits.isEmpty()) {
            System.out.println("No tasks processed this run? That's unexpected!");
            return;
        }

        // Basic math
        int n = waits.size();
        double elapsedSec = elapsedMs / 1000.0;
        double throughput = n / elapsedSec;

        // Wait times
        long minWait = Collections.min(waits);
        long maxWait = Collections.max(waits);
        double avgWait = average(waits);
        double stdWait = stdDev(waits, avgWait);

        // Turnaround times
        long minTAT = Collections.min(tats);
        long maxTAT = Collections.max(tats);
        double avgTAT = average(tats);
        double stdTAT = stdDev(tats, avgTAT);

        // Print to console
        System.out.println("\n----- RESULTS FOR THIS RUN -----");
        System.out.println("Tasks processed: " + n);
        System.out.printf("Total elapsed: %.2f s\n", elapsedSec);
        System.out.printf("Throughput: %.2f tasks/s\n", throughput);

        System.out.println("\nWait Time (ms):");
        System.out.println("  Min = " + minWait);
        System.out.println("  Max = " + maxWait);
        System.out.printf ("  Avg = %.2f\n", avgWait);
        System.out.printf ("  StdDev = %.2f\n", stdWait);

        System.out.println("\nTurnaround Time (ms):");
        System.out.println("  Min = " + minTAT);
        System.out.println("  Max = " + maxTAT);
        System.out.printf ("  Avg = %.2f\n", avgTAT);
        System.out.printf ("  StdDev = %.2f\n", stdTAT);
        System.out.println("------------------------------\n");

        // Append a line to the CSV so we keep track of all runs across program executions.
        appendCsv("runs.csv", System.currentTimeMillis(),
                elapsedSec, throughput,
                minWait, maxWait, avgWait, stdWait,
                minTAT, maxTAT, avgTAT, stdTAT);
    }

    // Simple average
    private static double average(List<Long> vals) {
        long sum = 0;
        for (long v : vals) sum += v;
        return (double) sum / vals.size();
    }

    // Simple standard deviation
    private static double stdDev(List<Long> vals, double mean) {
        double sumSq = 0.0;
        for (long v : vals) {
            double diff = v - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / vals.size());
    }

    // ----------------------------------------------------------------------------------
    // appendCsv: Adds one row of data to "runs.csv". We do not overwrite the file,
    // so repeated runs accumulate data. If the file doesn't exist, we create it & add a header.
    // ----------------------------------------------------------------------------------
    private static void appendCsv(
            String filePath,
            long timestamp,
            double secs,
            double tput,
            long minW, long maxW, double avgW, double stdW,
            long minT, long maxT, double avgT, double stdT
    ) {
        boolean alreadyExists = Files.exists(Paths.get(filePath));
        try (FileWriter fw = new FileWriter(filePath, true)) {
            // If file didn't exist before, write a header line.
            if (!alreadyExists) {
                fw.write("Timestamp,TotalTime(s),Throughput,MinWait,MaxWait,AvgWait,StdWait,MinTAT,MaxTAT,AvgTAT,StdTAT\n");
            }

            // Now append the data row
            fw.write(String.format(
                "%d,%.2f,%.2f,%d,%d,%.2f,%.2f,%d,%d,%.2f,%.2f\n",
                timestamp,
                secs,
                tput,
                minW,
                maxW,
                avgW,
                stdW,
                minT,
                maxT,
                avgT,
                stdT
            ));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
