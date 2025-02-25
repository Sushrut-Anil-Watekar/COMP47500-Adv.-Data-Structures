//package yourpackage; // (Optional) If using a package, adjust as needed.

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class TaskSchedulerExperiment {

    enum Priority {
        LOW, MEDIUM, HIGH
    }

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

            allWaitTimes.add(wait);
            allTurnaroundTimes.add(turnaround);

            System.out.println("Processed Task " + id + " (" + priority + ") | " +
                               "Wait: " + wait + "ms, " +
                               "Turnaround: " + turnaround + "ms, " +
                               "Thread: " + Thread.currentThread().getName());
        }

        public static List<Long> getAllWaitTimes() { return allWaitTimes; }
        public static List<Long> getAllTurnaroundTimes() { return allTurnaroundTimes; }

        // Clears the static lists so each run is independent.
        public static void resetMetrics() {
            allWaitTimes.clear();
            allTurnaroundTimes.clear();
        }
    }

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

    // We'll run the experiment multiple times and store each run's data in output/runs.csv
    public static void main(String[] args) {
        int totalRuns = 50; // or 100, or however many runs you want

        // Ensure our "output" folder exists
        File outputDir = new File("output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        for (int i = 1; i <= totalRuns; i++) {
            System.out.println("\n=============================");
            System.out.println("Starting run " + i + " of " + totalRuns);
            System.out.println("=============================");

            // Clear any leftover metrics before this run
            Task.resetMetrics();

            // Perform one run of the experiment
            singleRunExperiment();
        }

        System.out.println("\nAll " + totalRuns + " runs have completed.");
    }

    private static void singleRunExperiment() {
        long startMillis = System.currentTimeMillis();

        TaskScheduler scheduler = new TaskScheduler();
        Random rand = new Random();

        // Create 100 tasks, random priority
        for (int i = 1; i <= 100; i++) {
            int pick = rand.nextInt(3);
            Priority p = (pick == 0) ? Priority.LOW
                    : (pick == 1) ? Priority.MEDIUM : Priority.HIGH;

            Task newTask = new Task(i, p);
            scheduler.addTask(newTask);
        }

        // Start 3 threads, each dedicated to a priority
        ExecutorService pool = Executors.newFixedThreadPool(3);
        pool.execute(new Worker(scheduler, Priority.HIGH));
        pool.execute(new Worker(scheduler, Priority.MEDIUM));
        pool.execute(new Worker(scheduler, Priority.LOW));

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

        // Print & record stats
        computeAndRecordStats(elapsed);
    }

    private static void computeAndRecordStats(long elapsedMs) {
        List<Long> waits = Task.getAllWaitTimes();
        List<Long> tats = Task.getAllTurnaroundTimes();

        if (waits.isEmpty()) {
            System.out.println("No tasks processed this run. Strange!");
            return;
        }

        int n = waits.size();
        double elapsedSec = elapsedMs / 1000.0;
        double throughput = n / elapsedSec;

        long minWait = Collections.min(waits);
        long maxWait = Collections.max(waits);
        double avgWait = average(waits);
        double stdWait = stdDev(waits, avgWait);

        long minTAT = Collections.min(tats);
        long maxTAT = Collections.max(tats);
        double avgTAT = average(tats);
        double stdTAT = stdDev(tats, avgTAT);

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

        // Append this run's stats to 'output/runs.csv'
        String csvFilePath = "output/runs.csv";
        appendCsv(csvFilePath, System.currentTimeMillis(),
                  elapsedSec, throughput,
                  minWait, maxWait, avgWait, stdWait,
                  minTAT, maxTAT, avgTAT, stdTAT);
    }

    private static double average(List<Long> vals) {
        long sum = 0;
        for (long v : vals) sum += v;
        return (double) sum / vals.size();
    }

    private static double stdDev(List<Long> vals, double mean) {
        double sumSq = 0.0;
        for (long v : vals) {
            double diff = v - mean;
            sumSq += diff * diff;
        }
        return Math.sqrt(sumSq / vals.size());
    }

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
            // If file doesn't exist, write a header first.
            if (!alreadyExists) {
                fw.write("Timestamp,TotalTime(s),Throughput,MinWait,MaxWait,AvgWait,StdWait,MinTAT,MaxTAT,AvgTAT,StdTAT\n");
            }
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
