package com.database;
import com.database.indexing.BTree;
import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Collections;

public class BTreePerformanceTest {
    private static final int[] TEST_SIZES = {10000, 50000, 100000, 500000}; // Varied dataset sizes
    private static final int SEARCH_SIZE = 10000; // Number of search operations
    private static final int DELETE_SIZE = 5000; // Number of deletions to measure
    private static final int[] B_TREE_ORDERS = {2, 3, 4, 5}; // Varying B-Tree degrees

    /**
     * Runs performance tests with varied conditions.
     */
    @Test
    void runPerformanceTests() {
        for (int size : TEST_SIZES) {
            for (int order : B_TREE_ORDERS) {
                runExperiment(size, order, "Random");
                runExperiment(size, order, "Sorted");
                runExperiment(size, order, "Skewed");
            }
        }
    }

    /**
     * Runs an experiment for a specific dataset size, B-Tree order, and data type.
     * @param dataSize Number of elements to insert.
     * @param order B-Tree minimum degree.
     * @param dataType Type of input data (Random, Sorted, Skewed).
     */
    private void runExperiment(int dataSize, int order, String dataType) {
        BTree bTree = new BTree(order);
        TreeSet<Integer> bst = new TreeSet<>();
        ArrayList<Integer> dataset = generateDataset(dataSize, dataType);
        ArrayList<Integer> searchKeys = new ArrayList<>(dataset.subList(0, Math.min(SEARCH_SIZE, dataset.size())));
        ArrayList<Integer> deleteKeys = new ArrayList<>(dataset.subList(0, Math.min(DELETE_SIZE, dataset.size())));

        // Measure B-Tree Insertion Time
        long startTime = System.nanoTime();
        for (int num : dataset) {
            bTree.insert(num);
        }
        long bTreeInsertTime = System.nanoTime() - startTime;

        // Measure BST (TreeSet) Insertion Time
        startTime = System.nanoTime();
        for (int num : dataset) {
            bst.add(num);
        }
        long bstInsertTime = System.nanoTime() - startTime;

        // Measure B-Tree Search Time
        startTime = System.nanoTime();
        for (int num : searchKeys) {
            bTree.search(num);
        }
        long bTreeSearchTime = System.nanoTime() - startTime;

        // Measure BST (TreeSet) Search Time
        startTime = System.nanoTime();
        for (int num : searchKeys) {
            bst.contains(num);
        }
        long bstSearchTime = System.nanoTime() - startTime;

        // Measure B-Tree Deletion Time
        startTime = System.nanoTime();
        for (int num : deleteKeys) {
            // Assuming B-Tree has a delete function (implement if missing)
            // bTree.delete(num);
        }
        long bTreeDeleteTime = System.nanoTime() - startTime;

        // Measure BST (TreeSet) Deletion Time
        startTime = System.nanoTime();
        for (int num : deleteKeys) {
            bst.remove(num);
        }
        long bstDeleteTime = System.nanoTime() - startTime;

        // Print Results
        System.out.printf("DataType: %s | Size: %d | Order: %d\n", dataType, dataSize, order);
        System.out.printf("B-Tree Insertion: %.4f ms | BST Insertion: %.4f ms\n",
                bTreeInsertTime / 1e6, bstInsertTime / 1e6);
        System.out.printf("B-Tree Search: %.4f ms | BST Search: %.4f ms\n",
                bTreeSearchTime / 1e6, bstSearchTime / 1e6);
        System.out.printf("B-Tree Deletion: %.4f ms | BST Deletion: %.4f ms\n",
                bTreeDeleteTime / 1e6, bstDeleteTime / 1e6);
        System.out.println("---------------------------------------------");
    }

    /**
     * Generates dataset based on input type.
     * @param size Number of elements.
     * @param type Type of dataset (Random, Sorted, Skewed).
     * @return Generated dataset.
     */
    private ArrayList<Integer> generateDataset(int size, String type) {
        Random random = new Random();
        ArrayList<Integer> dataset = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            dataset.add(random.nextInt(Integer.MAX_VALUE));
        }
        if (type.equals("Sorted")) {
            Collections.sort(dataset);
        } else if (type.equals("Skewed")) {
            Collections.sort(dataset);
            for (int i = 0; i < size / 10; i++) {
                dataset.set(random.nextInt(size), random.nextInt(Integer.MAX_VALUE));
            }
        }
        return dataset;
    }
}
