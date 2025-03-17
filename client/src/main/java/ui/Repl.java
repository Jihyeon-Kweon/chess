package ui;

import java.util.Scanner;

public class Repl {
    private boolean running = true;

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("♔ Welcome to 240 Chess. Type 'help' to get started. ♔");

        while (running) {
            System.out.print(">>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            processCommand(input);
        }

        scanner.close();
    }

    private void processCommand(String input) {
        switch (input) {
            case "help":
                printHelp();
                break;
            case "quit":
                System.out.println("Exiting Chess Client...");
                running = false;
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("  help   - Show available commands.");
        System.out.println("  quit   - Exit the program.");
    }

    public static void main(String[] args) {
        new Repl().run();
    }
}
