package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import model.GameData;
import model.AuthData;
import dataaccess.AuthDAO;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import service.GameService;

@WebSocket
public class WebSocketHandler {

    private static GameService gameService;
    private static WebSocketCommunicator communicator;

    public static void init(GameService gs, WebSocketCommunicator comm) {
        gameService = gs;
        communicator = comm;
    }

    private static final Map<String, Session> userSessions = new ConcurrentHashMap<>();
    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String messageJson) {
        try {
            UserGameCommand command = gson.fromJson(messageJson, UserGameCommand.class);
            String authToken = command.getAuthToken();
            Integer gameID = command.getGameID();

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(authToken, gameID, session);
                case MAKE_MOVE -> handleMakeMove(authToken, gameID, messageJson, session);
                case LEAVE -> handleLeave(authToken, gameID);
                case RESIGN -> handleResign(authToken, gameID);
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error: Invalid message format or internal error");
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket closed: " + session);
        // TODO: 세션 정리 필요 시 처리
    }

    private void handleConnect(String authToken, Integer gameID, Session session) {
        try {
            System.out.println("🔐 handleConnect - authToken: " + authToken + ", gameID: " + gameID);

            AuthData authData = communicator.getAuthDAO().getAuth(authToken);
            System.out.println("🔍 Token lookup result: " + (authData == null ? "NOT FOUND" : authData.username()));

            ChessGame game = gameService.getGame(gameID, authToken);
            communicator.addConnection(authToken, session);

            LoadGameMessage loadGame = new LoadGameMessage(game);
            session.getRemote().sendString(gson.toJson(loadGame));

            String username = communicator.getUsername(authToken);
            String playerColor = getPlayerColor(gameID, username);
            String role = (playerColor != null) ? playerColor.toLowerCase() : "observer";
            String message = username + " connected as " + role;

            communicator.broadcast(authToken, gameID, new NotificationMessage(message));

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private String getPlayerColor(int gameID, String username) throws DataAccessException {
        var gameData = communicator.getGameDAO().getGame(gameID);
        if (username.equals(gameData.whiteUsername())) return "WHITE";
        if (username.equals(gameData.blackUsername())) return "BLACK";
        return null;
    }

    private void handleMakeMove(String authToken, Integer gameID, String json, Session session) {
        try {
            MakeMoveCommand command = gson.fromJson(json, MakeMoveCommand.class);

            AuthData authData = communicator.getAuthDAO().getAuth(authToken);
            if (authData == null) {
                sendError(session, "Error: invalid auth token");
                return;
            }

            ChessGame game = communicator.getGameDAO().getGame(gameID).game();
            if (game.isGameOver()) {
                sendError(session, "Error: game already over");
                return;
            }

            ChessGame updatedGame = gameService.makeMove(gameID, authToken, command.getMove());

            communicator.sendMessage(authToken, new LoadGameMessage(updatedGame));

            String username = communicator.getUsername(authToken);
            String msg = username + " made a move from " +
                    command.getMove().getStartPosition() + " to " +
                    command.getMove().getEndPosition();

            communicator.broadcast(authToken, gameID, new NotificationMessage(msg));
            communicator.broadcast(authToken, gameID, new LoadGameMessage(updatedGame));

        } catch (DataAccessException e) {
            e.printStackTrace();
            sendError(session, "Error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error: Invalid move command");
        }
    }

    private void sendErrorToToken(String authToken, String message) {
        communicator.sendMessage(authToken, new ErrorMessage(message));
    }

    private void handleLeave(String authToken, Integer gameID) {
        // TODO
    }

    private void handleResign(String authToken, Integer gameID) {
        try {
            System.out.println("\n🛑 [RESIGN REQUEST] Received resign request.");
            System.out.println("🔐 AuthToken: " + authToken + ", 🎮 GameID: " + gameID);

            AuthData authData = communicator.getAuthDAO().getAuth(authToken);
            if (authData == null) {
                System.out.println("❌ Invalid authToken: " + authToken);
                sendErrorToToken(authToken, "Error: invalid authToken");
                return;
            }

            GameData gameData = communicator.getGameDAO().getGame(gameID);
            if (gameData == null) {
                System.out.println("❌ Invalid gameID: " + gameID);
                sendErrorToToken(authToken, "Error: invalid game ID");
                return;
            }

            String username = authData.username();
            System.out.println("👤 Username: " + username);

            boolean isWhite = username.equals(gameData.whiteUsername());
            boolean isBlack = username.equals(gameData.blackUsername());

            if (!isWhite && !isBlack) {
                System.out.println("⚠️ Observer attempted to resign: " + username);
                sendErrorToToken(authToken, "Error: observers can't resign");
                return;
            }

            if (gameData.game().isGameOver()) {
                System.out.println("⚠️ Game already over. Resign rejected for: " + username);
                sendErrorToToken(authToken, "Error: game already over");
                return;
            }

            // ✅ 게임 종료 처리
            gameData.game().setGameOver(true);
            System.out.println("✅ Game marked as over.");

            String winner = isWhite ? gameData.blackUsername() : gameData.whiteUsername();
            String resignMessage = username + " resigned. " + winner + " wins.";
            System.out.println("📢 Notification: " + resignMessage);

            // ✅ NotificationMessage: 본인 제외, 모두에게 전송
            communicator.broadcastToGame(gameID, new NotificationMessage(resignMessage), authToken);

            // ✅ LoadGameMessage: 반대쪽 플레이어에게만 전송
            String otherUsername = winner;
            String otherToken = communicator.getAuthToken(otherUsername);

            System.out.println("📦 Sending LoadGameMessage to: " + otherUsername + " / token = " + otherToken);

            if (otherToken != null) {
                communicator.sendMessage(otherToken, new LoadGameMessage(gameData.game()));
                System.out.println("✅ LoadGameMessage successfully sent.");
            } else {
                System.out.println("⚠️ Could not find token for: " + otherUsername);
            }

            System.out.println("🛑 [RESIGN REQUEST COMPLETED]\n");

        } catch (DataAccessException e) {
            e.printStackTrace();
            sendErrorToToken(authToken, "Error: " + e.getMessage());
        }
    }




    private void sendError(Session session, String message) {
        try {
            String json = gson.toJson(new ErrorMessage(message));
            session.getRemote().sendString(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
