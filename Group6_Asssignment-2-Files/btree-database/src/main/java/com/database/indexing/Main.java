package com.database.indexing;

import java.util.Scanner;

/**
 * Main class to demonstrate the B-Tree functionality.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BTree bTree = new BTree(3);

        while (true) {
            System.out.println("\n1. Insert\n2. Search\n3. Traverse\n4. Delete\n5. Exit");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    System.out.print("Enter key to insert: ");
                    int key = scanner.nextInt();
                    bTree.insert(key);
                    break;
                case 2:
                    System.out.print("Enter key to search: ");
                    int searchKey = scanner.nextInt();
                    System.out.println("Found: " + bTree.search(searchKey));
                    break;
                case 3:
                    System.out.println("B-Tree Traversal: ");
                    bTree.traverse();
                    System.out.println();
                    break;
                case 4:
                    System.exit(0);
                default:
                    System.out.println("Invalid option, try again.");
            }
        }
    }
}