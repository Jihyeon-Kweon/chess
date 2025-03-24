package client;

import model.GameData;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ChessClient {
    private static final Scanner SCANNER = new Scanner(System.in);
    private static boolean isLoggedIn = false; // 로그인 상태 관리
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

    private static void handlePreloginCommands(String command, String[] tokens) {
        switch (command) {
            case "help":
                System.out.println("Available commands:");
                System.out.println("help - Show available commands");
                System.out.println("quit - Exit the program");
                System.out.println("register <USERNAME> <PASSWORD> <EMAIL> - Register a new account");
                System.out.println("login <USERNAME> <PASSWORD> - Log into an existing account");
                break;

            case "quit":
                System.out.println("Exiting program...");
                System.exit(0);
                break;

            case "register":
                if (tokens.length != 4) {
                    System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
                    break;
                }

                String username = tokens[1];
                String password = tokens[2];
                String email = tokens[3];

                if (SERVER_FACADE.registerUser(username, password, email)) {
                    System.out.println("Successfully registered!");

                    // 자동 로그인 시도
                    if (SERVER_FACADE.loginUser(username, password)) {
                        System.out.println("Logged in as " + username);
                        isLoggedIn = true;
                    } else {
                        System.out.println("But automatic login failed. Try logging in manually.");
                    }
                } else {
                    System.out.println("Error: Registration failed. Try a different username.");
                }
                break;


            case "login":
                if (tokens.length != 3) {
                    System.out.println("Usage: login <USERNAME> <PASSWORD>");
                    break;
                }
                if (SERVER_FACADE.loginUser(tokens[1], tokens[2])) {
                    System.out.println("Login successful!");
                    isLoggedIn = true; // 로그인 성공 시 상태 변경
                } else {
                    System.out.println("Error: Invalid username or password.");
                }
                break;



            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
                break;
        }
    }


    private static void handlePostloginCommands(String command, String[] tokens) {
        switch (command) {
            case "help":
                System.out.println("Available commands:");
                System.out.println("help - Show available commands");
                System.out.println("logout - Log out of your account");
                System.out.println("create <GAME_NAME> - Create a new game");
                System.out.println("list - List all available games");
                System.out.println("join <GAME_ID> <COLOR> - Join a game as white or black");
                System.out.println("observe <GAME_ID> - Observe a game");
                break;

            case "logout":
                System.out.println("Logging out...");
                isLoggedIn = false;
                break;

            case "create":
                if (tokens.length < 2) {
                    System.out.println("Usage: create <GAME_NAME>");
                    break;
                }

                // Extracting the full game name (in case of spaces)
                String gameName = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));

                if (SERVER_FACADE.createGame(gameName)) {
                    System.out.println("Game '" + gameName + "' created successfully!");
                } else {
                    System.out.println("Error: Failed to create game.");
                }
                break;

            case "list":
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
                break;

            case "join":
                if (tokens.length != 3) {
                    System.out.println("Usage: join <GAME_ID> <COLOR>");
                    break;
                }

                try {
                    int gameIndex = Integer.parseInt(tokens[1]);
                    String playerColor = tokens[2].toUpperCase();

                    List<GameData> joinGameList = SERVER_FACADE.listGames();
                    if (gameIndex < 1 || gameIndex > joinGameList.size()) {
                        System.out.println("Error: Invalid game ID.");
                        break;
                    }

                    int gameID = joinGameList.get(gameIndex - 1).gameID();

                    if (SERVER_FACADE.joinGame(gameID, playerColor)) {
                        System.out.println("Joined game successfully!");
                        // ✅ 보드 출력 추가
                        drawBoard(playerColor);
                    } else {
                        System.out.println("Failed to join game.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error: GAME_ID must be a number.");
                }
                break;



            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
                break;
        }
    }

    public static void drawBoard(String playerColor) {
        String[][] board = {
                {"♜", "♞", "♝", "♛", "♚", "♝", "♞", "♜"},
                {"♟", "♟", "♟", "♟", "♟", "♟", "♟", "♟"},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {" ", " ", " ", " ", " ", " ", " ", " "},
                {"♙", "♙", "♙", "♙", "♙", "♙", "♙", "♙"},
                {"♖", "♘", "♗", "♕", "♔", "♗", "♘", "♖"},
        };

        final String RESET = "\u001B[0m";
        final String RED = "\u001B[31m";
        final String BLUE = "\u001B[34m";

        int[] rowIndices = playerColor.equalsIgnoreCase("WHITE") ?
                new int[]{7,6,5,4,3,2,1,0} : new int[]{0,1,2,3,4,5,6,7};

        int[] colIndices = playerColor.equalsIgnoreCase("WHITE") ?
                new int[]{0,1,2,3,4,5,6,7} : new int[]{7,6,5,4,3,2,1,0};

        System.out.println();

        for (int row : rowIndices) {
            System.out.print((8 - row) + "  ");
            for (int col : colIndices) {
                String piece = board[row][col];
                if ("♟♜♞♝♛♚".contains(piece)) {
                    System.out.print(RED + piece + RESET + "  ");
                } else if ("♙♖♘♗♕♔".contains(piece)) {
                    System.out.print(BLUE + piece + RESET + "  ");
                } else {
                    System.out.print(piece + "  ");
                }
            }
            System.out.println((8 - row));
        }

        System.out.print("   ");
        for (int col : colIndices) {
            char file = (char) ('a' + col);
            System.out.print(" " + file + " ");
        }
        System.out.println("\n");
    }


}