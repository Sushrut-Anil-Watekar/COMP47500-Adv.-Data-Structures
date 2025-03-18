package com.database;

import com.database.indexing.AVLTree;
import com.database.indexing.BTree;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * JUnit 5 Test for AVL, Red-Black (TreeMap), and B-Tree Performance.
 * Measures insertion time, search time, memory usage, tree height, and node count.
 */
public class TreePerformanceTest {
    private static final int[] TEST_SIZES = {10000, 50000, 100000}; // Different dataset sizes for testing
    private static final int SEARCH_SIZE = 10000; // Number of keys used for search tests

    /**
     * Runs performance tests for AVL Tree, Red-Black Tree (TreeMap), and B-Tree.
     */
    @Test
    public void testPerformance() {
        for (int size : TEST_SIZES) {
            runExperiment(size, "Random"); // Test with randomly generated numbers
            runExperiment(size, "Sorted"); // Test with sorted numbers
            runExperiment(size, "Skewed"); // Test with slightly perturbed sorted numbers
        }
    }

    /**
     * Runs an experiment for a specific dataset size and type.
     * @param dataSize The number of elements in the dataset.
     * @param dataType The type of dataset (Random, Sorted, Skewed).
     */
    private void runExperiment(int dataSize, String dataType) {
        AVLTree avlTree = new AVLTree();
        TreeMap<Integer, Integer> rbTree = new TreeMap<>(); // Java's TreeMap (Red-Black Tree)
        BTree bTree = new BTree(3); // B-Tree with minimum degree 3

        ArrayList<Integer> dataset = generateDataset(dataSize, dataType);
        ArrayList<Integer> searchKeys = new ArrayList<>(dataset.subList(0, Math.min(SEARCH_SIZE, dataset.size())));

        // Measure memory usage before insertions
        long memoryBefore = getUsedMemory();

        // Measure insertion times for all tree types
        long avlInsertTime = measureInsertionTime(avlTree, dataset);
        long rbInsertTime = measureInsertionTime(rbTree, dataset);
        long bTreeInsertTime = measureInsertionTime(bTree, dataset);

        // Measure memory usage after insertions
        long memoryAfter = getUsedMemory();

        // Measure search times for all tree types
        long avlSearchTime = measureSearchTime(avlTree, searchKeys);
        long rbSearchTime = measureSearchTime(rbTree, searchKeys);
        long bTreeSearchTime = measureSearchTime(bTree, searchKeys);

        // Measure tree height and node count
        int avlHeight = avlTree.getHeight();
        int bTreeHeight = bTree.getHeight();
        int bTreeNodes = bTree.countNodes();

        // Print performance summary
        System.out.printf("\nDataType: %s | Size: %d\n", dataType, dataSize);
        System.out.printf("Insertion (ms) | AVL: %.4f | RB: %.4f | BTree: %.4f\n",
                avlInsertTime / 1e6, rbInsertTime / 1e6, bTreeInsertTime / 1e6);
        System.out.printf("Search (ms)    | AVL: %.4f | RB: %.4f | BTree: %.4f\n",
                avlSearchTime / 1e6, rbSearchTime / 1e6, bTreeSearchTime / 1e6);
        System.out.printf("Height         | AVL: %d | BTree: %d\n", avlHeight, bTreeHeight);
        System.out.printf("BTree Nodes    | %d\n", bTreeNodes);
        System.out.printf("Memory Used: %.4f MB\n", (memoryAfter - memoryBefore) / (1024.0 * 1024.0));
        System.out.println("------------------------------------------------------");
    }

    /**
     * Generates a dataset of given size with the specified type.
     * @param size Number of elements in the dataset.
     * @param type Type of dataset (Random, Sorted, Skewed).
     * @return Generated dataset as an ArrayList.
     */
    private ArrayList<Integer> generateDataset(int size, String type) {
        Random random = new Random();
        ArrayList<Integer> dataset = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            dataset.add(random.nextInt(Integer.MAX_VALUE));
        }
        if (type.equals("Sorted")) Collections.sort(dataset);
        else if (type.equals("Skewed")) {
            Collections.sort(dataset);
            for (int i = 0; i < size / 10; i++) {
                dataset.set(random.nextInt(size), random.nextInt(Integer.MAX_VALUE));
            }
        }
        return dataset;
    }

    /**
     * Measures the insertion time for an AVL Tree.
     */
    private long measureInsertionTime(AVLTree tree, ArrayList<Integer> dataset) {
        long startTime = System.nanoTime();
        for (int num : dataset) tree.insert(num);
        return System.nanoTime() - startTime;
    }

    /**
     * Measures the insertion time for a Red-Black Tree (TreeMap).
     */
    private long measureInsertionTime(TreeMap<Integer, Integer> tree, ArrayList<Integer> dataset) {
        long startTime = System.nanoTime();
        for (int num : dataset) tree.put(num, num);
        return System.nanoTime() - startTime;
    }

    /**
     * Measures the insertion time for a B-Tree.
     */
    private long measureInsertionTime(BTree tree, ArrayList<Integer> dataset) {
        long startTime = System.nanoTime();
        for (int num : dataset) tree.insert(num);
        return System.nanoTime() - startTime;
    }

    /**
     * Measures the search time for an AVL Tree.
     */
    private long measureSearchTime(AVLTree tree, ArrayList<Integer> searchKeys) {
        long startTime = System.nanoTime();
        for (int num : searchKeys) tree.search(num);
        return System.nanoTime() - startTime;
    }

    /**
     * Measures the search time for a Red-Black Tree (TreeMap).
     */
    private long measureSearchTime(TreeMap<Integer, Integer> tree, ArrayList<Integer> searchKeys) {
        long startTime = System.nanoTime();
        for (int num : searchKeys) tree.containsKey(num);
        return System.nanoTime() - startTime;
    }

    /**
     * Measures the search time for a B-Tree.
     */
    private long measureSearchTime(BTree tree, ArrayList<Integer> searchKeys) {
        long startTime = System.nanoTime();
        for (int num : searchKeys) tree.search(num);
        return System.nanoTime() - startTime;
    }

    /**
     * Returns the memory usage of the JVM before and after insertions.
     */
    private long getUsedMemory() {
        System.gc(); // Request garbage collection to get an accurate measurement
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }
}
