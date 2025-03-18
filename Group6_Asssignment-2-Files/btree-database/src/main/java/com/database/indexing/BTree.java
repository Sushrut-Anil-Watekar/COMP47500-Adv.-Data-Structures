package com.database.indexing;

/**
 * Represents a B-Tree data structure used for indexing.
 */
public class BTree {
    BTreeNode root;
    int degree;

    /**
     * Constructor to initialize the B-Tree with a given degree.
     * @param degree Minimum degree of the B-Tree (defines branching factor).
     */
    public BTree(int degree) {
        this.root = new BTreeNode(degree, true);
        this.degree = degree;
    }

    /**
     * Inserts a key into the B-Tree.
     * If the root is full, it is split, and a new root is created.
     * @param key The key to insert.
     */
    public void insert(int key) {
        if (root.numKeys == 2 * degree - 1) { // If root is full, split it
            BTreeNode newRoot = new BTreeNode(degree, false);
            newRoot.children[0] = root;
            newRoot.splitChild(0, root);
            root = newRoot;
        }
        root.insertNonFull(key); // Insert into the appropriate node
    }

    /**
     * Performs an in-order traversal of the B-Tree and prints the keys.
     */
    public void traverse() {
        if (root != null) root.traverse();
        System.out.println();
    }

    /**
     * Searches for a key in the B-Tree.
     * @param key The key to search for.
     * @return True if the key is found, false otherwise.
     */
    public boolean search(int key) {
        return root == null ? false : root.search(key) != null;
    }

    /**
     * Deletes a key from the B-Tree and adjusts the structure if necessary.
     * If the root becomes empty, it is replaced by its first child.
     * @param key The key to delete.
     */
    public void delete(int key) {
        if (root == null) {
            System.out.println("The tree is empty");
            return;
        }

        root.delete(key);

        // If root becomes empty, change root to its first child
        if (root.numKeys == 0) {
            if (root.isLeaf) {
                root = null; // The tree becomes empty
            } else {
                root = root.children[0]; // Promote the first child as new root
            }
        }
    }

    /**
     * Calculates the height of the B-Tree.
     * The height is the number of edges from the root to the deepest leaf.
     * @return The height of the tree.
     */
    public int getHeight() {
        int height = 0;
        BTreeNode current = root;
        while (current != null && !current.isLeaf) {
            height++;
            current = current.children[0]; // Move to the leftmost child
        }
        return height;
    }

    /**
     * Gets the number of nodes in the B-Tree.
     * @return The total number of nodes in the tree.
     */
    public int getNodeCount() {
        return getNodeCountRecursive(root);
    }

    /**
     * Recursively counts the number of nodes in the B-Tree.
     * @param node Current node in recursion.
     * @return Number of nodes in the subtree rooted at the given node.
     */
    private int getNodeCountRecursive(BTreeNode node) {
        if (node == null) return 0;
        int count = 1; // Count the current node
        if (!node.isLeaf) {
            for (int i = 0; i <= node.numKeys; i++) {
                count += getNodeCountRecursive(node.children[i]); // Count child nodes
            }
        }
        return count;
    }

    /**
     * Alias for getNodeCount() - counts total nodes in the B-Tree.
     * @return Total number of nodes in the tree.
     */
    public int countNodes() {
        return countNodesRecursive(root);
    }

    /**
     * Recursively counts the number of nodes in the B-Tree.
     * @param node Current node in recursion.
     * @return Total node count in the subtree rooted at the given node.
     */
    private int countNodesRecursive(BTreeNode node) {
        if (node == null) return 0;
        int count = 1; // Count the current node
        if (!node.isLeaf) {
            for (int i = 0; i <= node.numKeys; i++) {
                count += countNodesRecursive(node.children[i]); // Count child nodes
            }
        }
        return count;
    }
}