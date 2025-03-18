package client;

import java.util.Scanner;

public class ChessClient {
    private boolean loggedIn = false;
    private final ServerFacade server;

    public ChessClient(String serverUrl) {
        this.server = new ServerFacade(serverUrl);
    }

    public static void main(String[] args) {
        String serverUrl = "http://localhost:8080"; // Change this if your server runs on a different port
        if (args.length == 1) {
            serverUrl = args[0];
        }
        new ChessClient(serverUrl).run();
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(loggedIn ? "[LOGGED_IN] >>> " : "[LOGGED_OUT] >>> "); // Show login state
            String input = scanner.nextLine().trim();
            processCommand(input);
        }
    }

    private void processCommand(String input) {
        String[] parts = input.split(" ");

        if (parts.length == 0) return;

        switch (parts[0].toLowerCase()) {
            case "help":
                printHelp();
                break;
            case "quit":
                System.out.println("Exiting program...");
                System.exit(0);
                break;
            case "register":
                if (parts.length < 4) {
                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                    return;
                }
                register(parts[1], parts[2], parts[3]);
                break;
            case "login":
                if (parts.length < 3) {
                    System.out.println("Usage: login <USERNAME> <PASSWORD>");
                    return;
                }
                login(parts[1], parts[2]);
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    private void printHelp() {
        System.out.println("Available commands:");
        System.out.println("help - Show available commands");
        System.out.println("quit - Exit the program");
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - Register a new account");
        System.out.println("login <USERNAME> <PASSWORD> - Log into an existing account");
    }

    private void register(String username, String password, String email) {
        boolean success = server.registerUser(username, password, email);
        if (success) {
            System.out.println("Successfully registered! Please log in.");
        } else {
            System.out.println("Error: Registration failed. Try a different username.");
        }
    }

    private void login(String username, String password) {
        boolean success = server.loginUser(username, password);
        if (success) {
            loggedIn = true;
            System.out.println("Login successful!");
        } else {
            System.out.println("Error: Invalid username or password.");
        }
    }
}
