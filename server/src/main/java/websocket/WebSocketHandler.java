package websocket;

import chess.ChessGame;
import chess.InvalidMoveException;
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
    }

    private void handleConnect(String authToken, Integer gameID, Session session) {
        try {
            System.out.println("handleConnect - authToken: " + authToken + ", gameID: " + gameID);

            AuthData authData = communicator.getAuthDAO().getAuth(authToken);
            System.out.println("Token lookup result: " + (authData == null ? "NOT FOUND" : authData.username()));

            ChessGame game = gameService.getGame(gameID, authToken);
            communicator.addConnection(authToken, gameID, session);

            LoadGameMessage loadGame = new LoadGameMessage(game);
            session.getRemote().sendString(gson.toJson(loadGame));

            String username = communicator.getUsername(authToken);
            String playerColor = getPlayerColor(gameID, username);
            String role = (playerColor != null) ? playerColor.toLowerCase() : "observer";
            String message = username + " connected as " + role;

            communicator.broadcastToGame(gameID, new NotificationMessage(message), authToken);

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error: " + e.getMessage());
        }
    }

    private String getPlayerColor(int gameID, String username) throws DataAccessException {
        var gameData = communicator.getGameDAO().getGame(gameID);
        if (username.equals(gameData.whiteUsername())) {
            return "WHITE";
        }
        if (username.equals(gameData.blackUsername())) {
            return "BLACK";
        }
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

            ChessGame updatedGame;
            try {
                updatedGame = gameService.makeMove(gameID, authToken, command.getMove());
            } catch (InvalidMoveException e) {
                sendError(session, "Error: invalid move");
                return;
            } catch (DataAccessException e) {
                sendError(session, e.getMessage());
                return;
            }

            communicator.sendMessage(authToken, new LoadGameMessage(updatedGame));

            String username = communicator.getUsername(authToken);
            String msg = username + " made a move from " +
                    command.getMove().getStartPosition() + " to " +
                    command.getMove().getEndPosition();

            communicator.broadcastToGame(gameID, new NotificationMessage(msg), authToken);
            communicator.broadcastToGame(gameID, new LoadGameMessage(updatedGame), authToken);

        } catch (Exception e) {
            e.printStackTrace();
            sendError(session, "Error: Invalid move command");
        }
    }



    private void sendErrorToToken(String authToken, String message) {
        communicator.sendMessage(authToken, new ErrorMessage(message));
    }

    private void handleLeave(String authToken, Integer gameID) {
        try {
            String username = communicator.getUsername(authToken); // 먼저 유저 이름 가져오기

            gameService.leaveGame(gameID, authToken); // DB에서 유저 제거
            communicator.removeConnection(authToken); // 연결 제거

            // 모든 유저(플레이어 + 옵저버)에게 메시지 보내기
            String message = username + " left the game.";
            communicator.broadcastToGame(gameID, new NotificationMessage(message), null);

        } catch (DataAccessException e) {
            sendErrorToToken(authToken, "Error: " + e.getMessage());
        }
    }




    private void handleResign(String authToken, Integer gameID) {
        try {
            AuthData authData = communicator.getAuthDAO().getAuth(authToken);
            if (authData == null) {
                sendErrorToToken(authToken, "Error: invalid authToken");
                return;
            }

            GameData gameData = communicator.getGameDAO().getGame(gameID);
            if (gameData == null) {
                sendErrorToToken(authToken, "Error: invalid game ID");
                return;
            }

            String username = authData.username();
            boolean isWhite = username.equals(gameData.whiteUsername());
            boolean isBlack = username.equals(gameData.blackUsername());

            if (!isWhite && !isBlack) {
                sendErrorToToken(authToken, "Error: observers can't resign");
                return;
            }

            if (gameData.game().isGameOver()) {
                sendErrorToToken(authToken, "Error: game already over");
                return;
            }

            // 게임 종료 처리 후 DB 업데이트
            gameData.game().setGameOver(true);
            communicator.getGameDAO().updateGame(gameData);

            String winner = isWhite ? gameData.blackUsername() : gameData.whiteUsername();
            String resignMessage = username + " resigned. " + winner + " wins.";

            communicator.broadcastToGame(gameID, new NotificationMessage(resignMessage), null);

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