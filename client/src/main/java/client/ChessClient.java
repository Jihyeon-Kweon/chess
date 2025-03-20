package client;

import model.GameData;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ChessClient {
    private static final Scanner scanner = new Scanner(System.in);
    private static boolean isLoggedIn = false; // 로그인 상태 관리
    private static final ServerFacade serverFacade = new ServerFacade("http://localhost:8080");

    public static void main(String[] args) {
        while (true) {
            System.out.print(isLoggedIn ? "[LOGGED_IN] >>> " : "[LOGGED_OUT] >>> ");
            String input = scanner.nextLine().trim();
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
                if (serverFacade.registerUser(tokens[1], tokens[2], tokens[3])) {
                    System.out.println("Successfully registered! Please log in.");
                } else {
                    System.out.println("Error: Registration failed. Try a different username.");
                }
                break;

            case "login":
                if (tokens.length != 3) {
                    System.out.println("Usage: login <USERNAME> <PASSWORD>");
                    break;
                }
                if (serverFacade.loginUser(tokens[1], tokens[2])) {
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

                if (serverFacade.createGame(gameName)) {
                    System.out.println("Game '" + gameName + "' created successfully!");
                } else {
                    System.out.println("Error: Failed to create game.");
                }
                break;

            case "list":
                List<GameData> games = serverFacade.listGames();

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
                    int gameIndex = Integer.parseInt(tokens[1]); // 사용자 입력 인덱스 변환
                    String playerColor = tokens[2].toUpperCase();

                    // 게임 목록 가져오기
                    List<GameData> joinGameList = serverFacade.listGames();
                    if (gameIndex < 1 || gameIndex > joinGameList.size()) {
                        System.out.println("Error: Invalid game ID.");
                        break;
                    }

                    int gameID = joinGameList.get(gameIndex - 1).gameID(); // 올바른 gameID 가져오기

                    // 서버에 게임 참가 요청 보내기
                    if (serverFacade.joinGame(gameID, playerColor)) {
                        System.out.println("Joined game successfully!");
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

}