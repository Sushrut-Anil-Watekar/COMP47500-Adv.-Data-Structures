package com.database.indexing;

/**
 * AVL Tree implementation with balancing operations.
 */
public class AVLTree {
    private Node root;

    /**
     * Node structure for AVL Tree.
     */
    private static class Node {
        int key, height;
        Node left, right;

        /**
         * Constructor to initialize a node with a key.
         * @param key Key to be stored in the node.
         */
        public Node(int key) {
            this.key = key;
            this.height = 1; // New node is initially at height 1.
        }
    }

    /**
     * Inserts a key into the AVL tree.
     * @param key Key to be inserted.
     */
    public void insert(int key) {
        root = insert(root, key);
    }

    /**
     * Recursively inserts a key into the AVL tree and balances the tree.
     * @param node Current node in recursion.
     * @param key Key to be inserted.
     * @return Balanced node.
     */
    private Node insert(Node node, int key) {
        if (node == null) return new Node(key);

        // Insert key in the appropriate subtree
        if (key < node.key) node.left = insert(node.left, key);
        else if (key > node.key) node.right = insert(node.right, key);
        else return node; // Duplicate keys are not allowed

        // Update height of the current node
        node.height = 1 + Math.max(getHeight(node.left), getHeight(node.right));

        // Balance the node if required
        return balance(node);
    }

    /**
     * Searches for a key in the AVL tree.
     * @param key Key to be searched.
     * @return True if key is found, false otherwise.
     */
    public boolean search(int key) {
        return search(root, key) != null;
    }

    /**
     * Recursively searches for a key in the AVL tree.
     * @param node Current node in recursion.
     * @param key Key to be searched.
     * @return Node containing the key, or null if not found.
     */
    private Node search(Node node, int key) {
        if (node == null || node.key == key) return node;
        return key < node.key ? search(node.left, key) : search(node.right, key);
    }

    /**
     * Gets the height of the AVL tree.
     * @return Height of the tree.
     */
    public int getHeight() {
        return getHeight(root);
    }

    /**
     * Gets the height of a given node.
     * @param node Node whose height is to be fetched.
     * @return Height of the node, or 0 if null.
     */
    private int getHeight(Node node) {
        return node == null ? 0 : node.height;
    }

    /**
     * Performs in-order traversal of the AVL tree.
     */
    public void traverse() {
        traverse(root);
        System.out.println();
    }

    /**
     * Recursively performs in-order traversal of the AVL tree.
     * @param node Current node in recursion.
     */
    private void traverse(Node node) {
        if (node != null) {
            traverse(node.left);
            System.out.print(node.key + " ");
            traverse(node.right);
        }
    }

    /**
     * Balances the AVL tree after insertion.
     * @param node Node to be balanced.
     * @return Balanced node.
     */
    private Node balance(Node node) {
        int balanceFactor = getBalance(node);

        // Left-heavy case (Right rotation)
        if (balanceFactor > 1 && getBalance(node.left) >= 0)
            return rotateRight(node);

        // Right-heavy case (Left rotation)
        if (balanceFactor < -1 && getBalance(node.right) <= 0)
            return rotateLeft(node);

        // Left-Right case (Left-Right rotation)
        if (balanceFactor > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        // Right-Left case (Right-Left rotation)
        if (balanceFactor < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node; // Node is already balanced
    }

    /**
     * Computes the balance factor of a node.
     * @param node Node whose balance factor is to be calculated.
     * @return Balance factor of the node.
     */
    private int getBalance(Node node) {
        return node == null ? 0 : getHeight(node.left) - getHeight(node.right);
    }

    /**
     * Performs a right rotation on the given node.
     * @param y Node to be rotated.
     * @return New root after rotation.
     */
    private Node rotateRight(Node y) {
        Node x = y.left;
        Node T2 = x.right;

        // Rotation
        x.right = y;
        y.left = T2;

        // Update heights
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;

        return x; // Return new root
    }

    /**
     * Performs a left rotation on the given node.
     * @param x Node to be rotated.
     * @return New root after rotation.
     */
    private Node rotateLeft(Node x) {
        Node y = x.right;
        Node T2 = y.left;

        // Rotation
        y.left = x;
        x.right = T2;

        // Update heights
        x.height = Math.max(getHeight(x.left), getHeight(x.right)) + 1;
        y.height = Math.max(getHeight(y.left), getHeight(y.right)) + 1;

        return y; // Return new root
    }
}
