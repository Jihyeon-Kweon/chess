package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.commands.UserGameCommand.CommandType;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

@ClientEndpoint
public class ChessClient {

    private static final Scanner SCANNER = new Scanner(System.in);
    private static boolean isLoggedIn = false;
    private static final ServerFacade SERVER_FACADE = new ServerFacade("http://localhost:8080");
    private static Session webSocketSession;
    private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static String currentAuthToken;
    private static int currentGameID;
    private static String currentPlayerColor = "WHITE"; // 기본값

    public static void main(String[] args) {
        connectWebSocket();

        while (true) {
            System.out.print(isLoggedIn ? "[LOGGED_IN] >>> " : "[LOGGED_OUT] >>> ");
            String input = SCANNER.nextLine().trim();
            String[] tokens = input.split("\\s+");

            if (tokens.length == 0 || tokens[0].isEmpty()) continue;

            String command = tokens[0].toLowerCase();

            if (!isLoggedIn) handlePreloginCommands(command, tokens);
            else handlePostloginCommands(command, tokens);
        }
    }

    private static void connectWebSocket() {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = new URI("ws://localhost:8080/ws");
            container.connectToServer(ChessClient.class, uri);
        } catch (Exception e) {
            System.err.println("Failed to connect to WebSocket server.");
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("WebSocket connection established.");
        webSocketSession = session;
    }

    @OnMessage
    public void onMessage(String message) {
        try {
            var jsonObj = gson.fromJson(message, JsonObject.class);
            String type = jsonObj.get("serverMessageType").getAsString();

            switch (type) {
                case "NOTIFICATION" -> {
                    String note = jsonObj.get("message").getAsString();
                    System.out.println("\n[Notification] " + note);
                }

                case "LOAD_GAME" -> {
                    System.out.println("\n[Game Updated] Board received.");

                    // game 객체 파싱
                    JsonObject gameJson = jsonObj.getAsJsonObject("game");
                    ChessGame game = gson.fromJson(gameJson, ChessGame.class);

                    // 현재 로그인된 사용자 이름 가져오기
                    String currentUser = SERVER_FACADE.getCurrentUsername();
                    String perspective = "WHITE"; // 기본값

                    // 현재 GameData에서 WHITE or BLACK 여부 확인
                    List<GameData> games = SERVER_FACADE.listGames();
                    GameData currentGame = games.stream()
                            .filter(g -> g.gameID() == currentGameID)
                            .findFirst()
                            .orElse(null);

                    if (currentGame != null && currentUser != null) {
                        if (currentUser.equals(currentGame.blackUsername())) {
                            perspective = "BLACK";
                        } else if (currentUser.equals(currentGame.whiteUsername())) {
                            perspective = "WHITE";
                        } else {
                            perspective = "WHITE"; // 관전자도 WHITE 기준으로 봄
                        }
                    }

                    // 보드 출력
                    ChessClientUtils.drawBoard(game, perspective);
                }

                case "ERROR" -> {
                    // "errorMessage" 또는 "message" 둘 중 하나가 있는지 확인
                    String error = jsonObj.has("errorMessage")
                            ? jsonObj.get("errorMessage").getAsString()
                            : jsonObj.get("message").getAsString();  // fallback
                    System.out.println("\n[Error] " + error);
                }


                default -> {
                    System.out.println("\n[WebSocket] Unknown message type:");
                    System.out.println(message);
                }
            }
        } catch (Exception e) {
            System.out.println("\n[WebSocket] Failed to parse message:");
            e.printStackTrace();
            System.out.println(message);
        }

        System.out.print(isLoggedIn ? "[LOGGED_IN] >>> " : "[LOGGED_OUT] >>> ");
    }


    @OnClose
    public void onClose(Session session, CloseReason closeReason) {
        System.out.println("WebSocket closed: " + closeReason.getReasonPhrase());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error:");
        throwable.printStackTrace();
    }

    private static void handlePreloginCommands(String command, String[] tokens) {
        switch (command) {
            case "help" -> printPreLoginHelp();
            case "quit" -> quit();
            case "register" -> register(tokens);
            case "login" -> login(tokens);
            default -> System.out.println("Unknown command. Type 'help' for help.");
        }
    }

    private static void handlePostloginCommands(String command, String[] tokens) {
        switch (command) {
            case "help" -> printPostLoginHelp();
            case "logout" -> logout();
            case "create" -> createGame(tokens);
            case "list" -> listGames();
            case "join" -> joinGame(tokens);
            case "observe" -> observeGame(tokens);
            case "move" -> makeMove(tokens);
            case "leave" -> leaveGame();
            case "resign" -> resignGame();
            default -> System.out.println("Unknown command. Type 'help' for help.");
        }
    }

    private static void printPreLoginHelp() {
        System.out.println("Commands: help, quit, register <USERNAME> <PASSWORD> <EMAIL>, login <USERNAME> <PASSWORD>");
    }

    private static void printPostLoginHelp() {
        System.out.println("Commands: help, logout, create <GAME_NAME>, list, join <GAME_ID> <COLOR>, observe <GAME_ID>, move <START> <END>, leave, resign");
    }

    private static void quit() {
        System.out.println("Exiting...");
        System.exit(0);
    }

    private static void register(String[] tokens) {
        if (tokens.length != 4) {
            System.out.println("Usage: register <USERNAME> <PASSWORD> <EMAIL>");
            return;
        }
        if (SERVER_FACADE.registerUser(tokens[1], tokens[2], tokens[3])) {
            System.out.println("Registered successfully!");
            login(new String[]{"login", tokens[1], tokens[2]});
        } else {
            System.out.println("Registration failed.");
        }
    }

    private static void login(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Usage: login <USERNAME> <PASSWORD>");
            return;
        }
        if (SERVER_FACADE.loginUser(tokens[1], tokens[2])) {
            System.out.println("Login successful!");
            currentAuthToken = SERVER_FACADE.getAuthToken();
            isLoggedIn = true;
        } else {
            System.out.println("Login failed.");
        }
    }

    private static void logout() {
        System.out.println("Logged out.");
        isLoggedIn = false;
        currentAuthToken = null;
    }

    private static void createGame(String[] tokens) {
        if (tokens.length < 2) {
            System.out.println("Usage: create <GAME_NAME>");
            return;
        }
        String name = String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length));
        if (SERVER_FACADE.createGame(name)) {
            System.out.println("Game created.");
        } else {
            System.out.println("Failed to create game.");
        }
    }

    private static void listGames() {
        List<GameData> games = SERVER_FACADE.listGames();
        if (games.isEmpty()) System.out.println("No games.");
        else {
            int i = 1;
            for (GameData game : games) {
                System.out.printf("%d. %s (White: %s, Black: %s)%n", i++, game.gameName(),
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
            int index = Integer.parseInt(tokens[1]);
            String color = tokens[2].toUpperCase();
            List<GameData> games = SERVER_FACADE.listGames();
            if (index < 1 || index > games.size()) {
                System.out.println("Invalid game index.");
                return;
            }
            int gameID = games.get(index - 1).gameID();
            currentGameID = gameID;
            currentPlayerColor = color;
            if (SERVER_FACADE.joinGame(gameID, color)) {
                System.out.println("Joined game!");
                sendCommand(new UserGameCommand(CommandType.CONNECT, currentAuthToken, gameID));
            } else {
                System.out.println("Join failed.");
            }
        } catch (NumberFormatException e) {
            System.out.println("GAME_ID must be a number.");
        }
    }

    private static void observeGame(String[] tokens) {
        if (tokens.length != 2) {
            System.out.println("Usage: observe <GAME_ID>");
            return;
        }
        try {
            int index = Integer.parseInt(tokens[1]);
            List<GameData> games = SERVER_FACADE.listGames();
            if (index < 1 || index > games.size()) {
                System.out.println("Invalid game index.");
                return;
            }
            int gameID = games.get(index - 1).gameID();
            currentGameID = gameID;
            currentPlayerColor = "WHITE"; // default 관전자 시야
            sendCommand(new UserGameCommand(CommandType.CONNECT, currentAuthToken, gameID));
        } catch (NumberFormatException e) {
            System.out.println("GAME_ID must be a number.");
        }
    }

    private static void makeMove(String[] tokens) {
        if (tokens.length != 3) {
            System.out.println("Usage: move <START> <END>");
            return;
        }
        try {
            ChessPosition start = parsePosition(tokens[1]);
            ChessPosition end = parsePosition(tokens[2]);
            ChessMove move = new ChessMove(start, end, null);  // 승격은 null 처리
            MakeMoveCommand cmd = new MakeMoveCommand(CommandType.MAKE_MOVE, currentAuthToken, currentGameID, move);
            sendCommand(cmd);
        } catch (Exception e) {
            System.out.println("Invalid move format.");
        }
    }

    private static ChessPosition parsePosition(String input) {
        input = input.toLowerCase();
        if (input.length() != 2) throw new IllegalArgumentException("Invalid position: " + input);
        int col = input.charAt(0) - 'a' + 1;
        int row = input.charAt(1) - '0';
        return new ChessPosition(row, col);
    }

    private static void leaveGame() {
        sendCommand(new UserGameCommand(CommandType.LEAVE, currentAuthToken, currentGameID));
        System.out.println("Left the game.");
    }

    private static void resignGame() {
        System.out.print("Are you sure you want to resign? (Y/N): ");
        String input = SCANNER.nextLine().trim().toLowerCase();

        if (input.equals("y") || input.equals("yes")) {
            sendCommand(new UserGameCommand(CommandType.RESIGN, currentAuthToken, currentGameID));
            System.out.println("You have resigned from the game.");
        } else {
            System.out.println("Resignation canceled.");
        }
    }


    private static void sendCommand(Object command) {
        try {
            String json = gson.toJson(command);
            webSocketSession.getBasicRemote().sendText(json);
        } catch (IOException e) {
            System.out.println("Failed to send command.");
            e.printStackTrace();
        }
    }

    private static String getPerspective(ChessGame game) {
        try {
            // 현재 사용자 이름
            String currentUser = SERVER_FACADE.getUsername(currentAuthToken);
            // gameData에서 color 확인 (직접 구현해도 OK)
            GameData data = SERVER_FACADE.listGames().stream()
                    .filter(g -> g.gameID() == currentGameID)
                    .findFirst()
                    .orElse(null);
            if (data != null) {
                if (currentUser.equals(data.whiteUsername())) return "WHITE";
                if (currentUser.equals(data.blackUsername())) return "BLACK";
            }
        } catch (Exception ignored) {}
        return "WHITE"; // default to WHITE perspective
    }

}
