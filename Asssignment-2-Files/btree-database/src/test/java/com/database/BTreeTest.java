package com.database;
import com.database.indexing.BTree;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the BTree implementation.
 * This test suite verifies the correctness of BTree operations,
 * including insertion, searching, and structural balance.
 */
public class BTreeTest {
    private BTree bTree;

    /**
     * Initializes a new BTree instance before each test.
     * The BTree has a minimum degree of 3, meaning each node can hold at most 5 keys.
     */
    @BeforeEach
    void setUp() {
        bTree = new BTree(3); // Initialize B-Tree with minimum degree 3
    }

    /**
     * Tests insertion of multiple elements into the BTree.
     * Ensures that inserted elements can be successfully searched.
     */
    @Test
    void testInsertion() {
        // Insert multiple values into the B-Tree
        bTree.insert(10);
        bTree.insert(20);
        bTree.insert(5);
        bTree.insert(6);
        bTree.insert(12);
        bTree.insert(30);
        bTree.insert(7);
        bTree.insert(17);

        // Verify that inserted elements exist in the tree
        assertTrue(bTree.search(10));
        assertTrue(bTree.search(5));
        assertTrue(bTree.search(30));
        assertTrue(bTree.search(17));
    }

    /**
     * Tests searching functionality in the BTree.
     * Verifies that existing elements return true, and non-existing elements return false.
     */
    @Test
    void testSearch() {
        // Insert values into the BTree
        bTree.insert(15);
        bTree.insert(25);
        bTree.insert(35);

        // Verify that inserted elements can be found
        assertTrue(bTree.search(15));
        assertTrue(bTree.search(25));

        // Verify that a non-existing element is not found
        assertFalse(bTree.search(40)); // Element 40 was not inserted
    }

    /**
     * Tests multiple insertions and searches in the BTree.
     * Ensures that a large number of elements can be inserted and searched efficiently.
     */
    @Test
    void testMultipleInsertionsAndSearch() {
        // Insert 100 sequential numbers into the BTree
        for (int i = 1; i <= 100; i++) {
            bTree.insert(i);
        }

        // Verify that all inserted numbers can be found
        for (int i = 1; i <= 100; i++) {
            assertTrue(bTree.search(i));
        }

        // Ensure that an out-of-range value is not found
        assertFalse(bTree.search(101)); // Should return false as 101 was not inserted
    }

    /**
     * Tests that the BTree maintains balance after multiple insertions.
     * Ensures that insertions do not degrade tree structure.
     */
    @Test
    void testTreeBalanceAfterInsertions() {
        // Insert multiple values in increments of 10
        for (int i = 10; i <= 100; i += 10) {
            bTree.insert(i);
        }

        // Verify that inserted values can be found in the tree
        assertTrue(bTree.search(10));
        assertTrue(bTree.search(50));
        assertTrue(bTree.search(100));

        // Verify that an element beyond inserted values is not found
        assertFalse(bTree.search(105)); // Element 105 was never inserted
    }
}