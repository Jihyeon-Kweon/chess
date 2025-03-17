package ui;

import client.ServerFacade;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Scanner;

public class Repl {
    private boolean running = true;
    private final ServerFacade server;
    private String authToken = null;

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
            case "listGames":
                handleListGames();
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
            System.out.println("✅ Registration successful: " + response);
        } catch (Exception e) {
            System.out.println("❌ Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin(Scanner scanner) {
        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Enter password: ");
        String password = scanner.nextLine().trim();

        try {
            String response = server.login(username, password);
            var jsonResponse = new Gson().fromJson(response, Map.class);
            authToken = (String) jsonResponse.get("authToken"); // 토큰 저장
            System.out.println("✅ Login successful: " + response);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private void handleListGames() {
        if (authToken == null) {
            System.out.println("❌ You must be logged in to list games.");
            return;
        }

        try {
            String response = server.listGames(authToken);
            System.out.println("🎲 Available Games: " + response);
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