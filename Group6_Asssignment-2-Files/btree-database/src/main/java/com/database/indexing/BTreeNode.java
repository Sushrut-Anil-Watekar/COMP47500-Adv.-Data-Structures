package com.database.indexing;

import java.util.Arrays;
import java.util.Scanner;

/**
 * Represents a node in a B-Tree.
 * A B-Tree node contains multiple keys and child pointers.
 * Each node can have a maximum of (2 * degree - 1) keys.
 */
class BTreeNode {
    int[] keys;  // Array to store keys in sorted order
    int degree;  // Minimum degree of the B-Tree (defines range for keys)
    BTreeNode[] children;  // Array to store child nodes
    int numKeys;  // Current number of keys in the node
    boolean isLeaf;  // True if the node is a leaf node (has no children)

    /**
     * Constructor to initialize a BTreeNode.
     * @param degree Minimum degree of the B-Tree.
     * @param isLeaf Boolean indicating if the node is a leaf.
     */
    public BTreeNode(int degree, boolean isLeaf) {
        this.degree = degree;
        this.isLeaf = isLeaf;
        this.keys = new int[2 * degree - 1]; // Maximum number of keys a node can hold
        this.children = new BTreeNode[2 * degree]; // Maximum number of children a node can have
        this.numKeys = 0; // Initialize node with zero keys
    }

    /**
     * Searches for a key in the B-Tree.
     * @param key The key to search for.
     * @return The node containing the key, or null if not found.
     */
    public BTreeNode search(int key) {
        int i = 0;

        // Find the first key greater than or equal to key
        while (i < numKeys && key > keys[i]) i++;

        // If the key is found, return this node
        if (i < numKeys && keys[i] == key) return this;

        // If this is a leaf node, key is not present
        if (isLeaf) return null;

        // Recur to the appropriate child
        return children[i].search(key);
    }


    public int countNodes() {
        int count = numKeys; // Count keys in this node
        for (int i = 0; i <= numKeys; i++) {
            if (!isLeaf && children[i] != null) {
                count += children[i].countNodes(); // Recursively count keys in children
            }
        }
        return count;
    }

    /**
     * Traverses and prints all keys in the B-Tree in sorted order.
     * This is a recursive function that performs an in-order traversal.
     **/
    public void traverse() {
        for (int i = 0; i < numKeys; i++) {
            // Recursively traverse the left subtree before printing the key
            if (!isLeaf) children[i].traverse();
            System.out.print(keys[i] + " ");
        }
        // Recursively traverse the right-most child
        if (!isLeaf) children[numKeys].traverse();
    }

    /**
    public void traverseTime() {
        System.out.print("Staring Traverse");
        for (int i = 0; i < numKeys; i++) {
            // Recursively traverse the left subtree before printing the key
            if (!isLeaf) children[i].traverse();

        } System.out.print("Ending");
        // Recursively traverse the right-most child
        if (!isLeaf) children[numKeys].traverse();
    }
     **/


    public void insertNonFull(int key) {
        int i = numKeys - 1;
        if (isLeaf) {
            while (i >= 0 && keys[i] > key) {
                keys[i + 1] = keys[i];
                i--;
            }
            keys[i + 1] = key;
            numKeys++;
        } else {
            while (i >= 0 && keys[i] > key) i--;
            if (children[i + 1].numKeys == 2 * degree - 1) {
                splitChild(i + 1, children[i + 1]);
                if (keys[i + 1] < key) i++;
            }
            children[i + 1].insertNonFull(key);
        }
    }

    public void splitChild(int i, BTreeNode y) {
        BTreeNode z = new BTreeNode(y.degree, y.isLeaf);
        z.numKeys = degree - 1;
        System.arraycopy(y.keys, degree, z.keys, 0, degree - 1);
        if (!y.isLeaf) {
            System.arraycopy(y.children, degree, z.children, 0, degree);
        }
        for (int j = numKeys; j >= i + 1; j--) {
            children[j + 1] = children[j];
        }
        children[i + 1] = z;
        for (int j = numKeys - 1; j >= i; j--) {
            keys[j + 1] = keys[j];
        }
        keys[i] = y.keys[degree - 1];
        numKeys++;
        y.numKeys = degree - 1;
    }

    private void merge(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];
        child.keys[degree - 1] = keys[idx];
        System.arraycopy(sibling.keys, 0, child.keys, degree, sibling.numKeys);
        if (!child.isLeaf) {
            System.arraycopy(sibling.children, 0, child.children, degree, sibling.numKeys + 1);
        }
        for (int i = idx; i < numKeys - 1; i++) {
            keys[i] = keys[i + 1];
            children[i + 1] = children[i + 2];
        }
        numKeys--;
        child.numKeys += sibling.numKeys + 1;
    }

    private void borrowFromPrev(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx - 1];
        for (int i = child.numKeys - 1; i >= 0; i--) {
            child.keys[i + 1] = child.keys[i];
        }
        if (!child.isLeaf) {
            for (int i = child.numKeys; i >= 0; i--) {
                child.children[i + 1] = child.children[i];
            }
        }
        child.keys[0] = keys[idx - 1];
        if (!child.isLeaf) {
            child.children[0] = sibling.children[sibling.numKeys];
        }
        keys[idx - 1] = sibling.keys[sibling.numKeys - 1];
        child.numKeys++;
        sibling.numKeys--;
    }

    private void borrowFromNext(int idx) {
        BTreeNode child = children[idx];
        BTreeNode sibling = children[idx + 1];
        child.keys[child.numKeys] = keys[idx];
        if (!child.isLeaf) {
            child.children[child.numKeys + 1] = sibling.children[0];
        }
        keys[idx] = sibling.keys[0];
        for (int i = 1; i < sibling.numKeys; i++) {
            sibling.keys[i - 1] = sibling.keys[i];
        }
        if (!sibling.isLeaf) {
            for (int i = 1; i <= sibling.numKeys; i++) {
                sibling.children[i - 1] = sibling.children[i];
            }
        }
        child.numKeys++;
        sibling.numKeys--;
    }

    private void fill(int idx) {
        if (idx != 0 && children[idx - 1].numKeys >= degree) {
            borrowFromPrev(idx);
        } else if (idx != numKeys && children[idx + 1].numKeys >= degree) {
            borrowFromNext(idx);
        } else {
            if (idx != numKeys) {
                merge(idx);
            } else {
                merge(idx - 1);
            }
        }
    }

    public void delete(int key) {
        int idx = 0;
        while (idx < numKeys && keys[idx] < key) {
            idx++;
        }
        if (idx < numKeys && keys[idx] == key) {
            if (isLeaf) {
                removeFromLeaf(idx);
            } else {
                removeFromNonLeaf(idx);
            }
        } else {
            if (isLeaf) {
                System.out.println("Key " + key + " not found in the tree.");
                return;
            }
            boolean lastChild = (idx == numKeys);
            if (children[idx].numKeys < degree) {
                fill(idx);
            }
            if (lastChild && idx > numKeys) {
                children[idx - 1].delete(key);
            } else {
                children[idx].delete(key);
            }
        }
    }

    private void removeFromLeaf(int idx) {
        for (int i = idx; i < numKeys - 1; i++) {
            keys[i] = keys[i + 1];
        }
        numKeys--;
    }

    private void removeFromNonLeaf(int idx) {
        int key = keys[idx];
        if (children[idx].numKeys >= degree) {
            int pred = getPredecessor(idx);
            keys[idx] = pred;
            children[idx].delete(pred);
        } else if (children[idx + 1].numKeys >= degree) {
            int succ = getSuccessor(idx);
            keys[idx] = succ;
            children[idx + 1].delete(succ);
        } else {
            merge(idx);
            children[idx].delete(key);
        }
    }

    private int getPredecessor(int idx) {
        BTreeNode current = children[idx];
        while (!current.isLeaf) {
            current = current.children[current.numKeys];
        }
        return current.keys[current.numKeys - 1];
    }

    private int getSuccessor(int idx) {
        BTreeNode current = children[idx + 1];
        while (!current.isLeaf) {
            current = current.children[0];
        }
        return current.keys[0];
    }


}
