package client;

import model.GameData;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class ChessClient {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static final ServerFacade SERVER_FACADE = new ServerFacade("http://localhost:8080");

    public static void main(String[] args) {
        while (true) {
            System.out.print(isLoggedIn ? "[LOGGED_IN] >>> " : "[LOGGED_OUT] >>> ");
            String input = SCANNER.nextLine().trim();
            String[] tokens = input.split("\\s+");

            if (tokens.length == 0 || tokens[0].isEmpty()) {
                continue;
            }

            String command = tokens[0].toLowerCase();

            if (!isLoggedIn) {
                handlePreloginCommands(command, tokens);
            } else {
                handlePostloginCommands(command, tokens);
            }
        }
    }

    // ----------- Pre-login Commands ----------- //
    private static void handlePreloginCommands(String command, String[] tokens) {
        switch (command) {
            case "help" -> printPreLoginHelp();
            case "quit" -> quit();
            case "register" -> register(tokens);
            case "login" -> login(tokens);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    // ----------- Post-login Commands ----------- //
    private static void handlePostloginCommands(String command, String[] tokens) {
        switch (command) {
            case "help" -> printPostLoginHelp();
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "join" -> joinGame(tokens);
            case "observe" -> observeGame(tokens);
            default -> System.out.println("Unknown command. Type 'help' for available commands.");
        }
    }

    // ----------- Command Handlers ----------- //
    private static void printPreLoginHelp() {
        System.out.println("Available commands:");
        System.out.println("help - Show available commands");
        System.out.println("quit - Exit the program");
        System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - Register a new account");
        System.out.println("login <USERNAME> <PASSWORD> - Log into an existing account");
    }

    private static void printPostLoginHelp() {
        System.out.println("Available commands:");
        System.out.println("help - Show available commands");
        System.out.println("logout - Log out of your account");
        System.out.println("create <GAME_NAME> - Create a new game");
        System.out.println("list - List all available games");
        System.out.println("join <GAME_ID> <COLOR> - Join a game as white or black");
        System.out.println("observe <GAME_ID> - Observe a game");
    }

    private static void quit() {
        System.out.println("Exiting program...");
        System.exit(0);
    }

    private static void register(String[] tokens) {
        if (tokens.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        String username = tokens[1];
        String password = tokens[2];
        String email = tokens[3];

        if (SERVER_FACADE.registerUser(username, password, email)) {
            System.out.println("Successfully registered!");
            if (SERVER_FACADE.loginUser(username, password)) {
                System.out.println("Logged in as " + username);
                isLoggedIn = true;
            } else {
                System.out.println("But automatic login failed. Try logging in manually.");
            }
        } else {
            System.out.println("Error: Registration failed. Try a different username.");
        }
    }

    private static void login(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }
        if (SERVER_FACADE.loginUser(tokens[1], tokens[2])) {
            System.out.println("Login successful!");
            isLoggedIn = true;
        } else {
            System.out.println("Error: Invalid username or password.");
        }
    }

    private static void logout() {
        System.out.println("Logging out...");
        isLoggedIn = false;
    }

    private static void createGame(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: create <GAME_NAME>");
            return;
        }
        String gameName = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
        if (SERVER_FACADE.createGame(gameName)) {
            System.out.println("Game '" + gameName + "' created successfully!");
        } else {
            System.out.println("Error: Failed to create game.");
        }
    }

    private static void listGames() {
        List<GameData> games = SERVER_FACADE.listGames();
        if (games.isEmpty()) {
            System.out.println("No games available.");
        } else {
            System.out.println("Available games:");
            int index = 1;
            for (GameData game : games) {
                System.out.printf("%d. %s (White: %s, Black: %s)%n",
                        index++, game.gameName(),
                        game.whiteUsername() == null ? "Open" : game.whiteUsername(),
                        game.blackUsername() == null ? "Open" : game.blackUsername());
            }
        }
    }

    private static void joinGame(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Usage: join <GAME_ID> <COLOR>");
            return;
        }
        try {
            int gameIndex = Integer.parseInt(tokens[1]);
            String playerColor = tokens[2].toUpperCase();

            List<GameData> games = SERVER_FACADE.listGames();
            if (gameIndex < 1 || gameIndex > games.size()) {
                System.out.println("Error: Invalid game ID.");
                return;
            }

            int gameID = games.get(gameIndex - 1).gameID();
            if (SERVER_FACADE.joinGame(gameID, playerColor)) {
                System.out.println("Joined game successfully!");
                ChessClientUtils.drawBoard(playerColor);
            } else {
                System.out.println("Failed to join game.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: GAME_ID must be a number.");
        }
    }

    private static void observeGame(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: observe <GAME_ID>");
            return;
        }
        try {
            int gameIndex = Integer.parseInt(tokens[1]);
            List<GameData> games = SERVER_FACADE.listGames();
            if (gameIndex < 1 || gameIndex > games.size()) {
                System.out.println("Error: Invalid game ID.");
                return;
            }

            int gameID = games.get(gameIndex - 1).gameID();
            ChessClientUtils.observeGame(gameID);
        } catch (NumberFormatException e) {
            System.out.println("Error: GAME_ID must be a number.");
        }
    }
}
