package com.taskscheduler;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

public class SystemTest {

    @Test
    public void testFullExperimentRun() throws Exception {
        // Ensure the output directory is clean
        File outputDir = new File("output");
        if (outputDir.exists()) {
            for (File file : outputDir.listFiles()) {
                file.delete();
            }
        }

        // Run the experiment
        TaskSchedulerExperiment.main(new String[]{});

        // Check if the CSV file was created
        File csvFile = new File("output/runs.csv");
        assertTrue("CSV output file should exist", csvFile.exists());

        // Ensure the file is not empty (Fix for Java 8)
        String content = new String(Files.readAllBytes(Paths.get("output/runs.csv")), StandardCharsets.UTF_8);
        assertTrue("CSV file should contain results", content.length() > 0);
    }
}
