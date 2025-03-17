package ui;

import client.ServerFacade;
import java.util.Scanner;

public class Repl {
    private boolean running = true;
    private ServerFacade server;

    public Repl(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("♔ Welcome to 240 Chess. Type 'help' to get started. ♔");

        while (running) {
            System.out.print(">>> ");
            String input = scanner.nextLine().trim().toLowerCase();
            processCommand(input, scanner);
        }

        scanner.close();
    }

    private void processCommand(String input, Scanner scanner) {
        switch (input) {
            case "help":
                printHelp();
                break;
            case "quit":
                System.out.println("Exiting Chess Client...");
                running = false;
                break;
            case "register":
                handleRegister(scanner);
                break;
            case "login":
                handleLogin(scanner);
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void handleRegister(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        try {
            String response = server.register(username, password, email);
            System.out.println("Registration successful: " + response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        try {
            String response = server.login(username, password);
            System.out.println("Login successful: " + response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help       - Show available commands.");
        System.out.println("  quit       - Exit the program.");
        System.out.println("  register   - Register a new account.");
        System.out.println("  login      - Login to your account.");
        System.out.println("\nMore commands will be added in the future.\n");
    }


    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080"; // 서버 주소
        new Repl(serverUrl).run();
    }
}
