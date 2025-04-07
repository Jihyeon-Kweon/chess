package websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.DataAccessException;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
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

            // CONNECT, MAKE_MOVE, etc 처리
            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(authToken, gameID, session);
                case MAKE_MOVE -> handleMakeMove(authToken, gameID, messageJson);
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
            communicator.addConnection(authToken, session);

            // 1. getGame()은 ChessGame을 반환하므로 바로 받기
            ChessGame game = gameService.getGame(gameID, authToken);

            // 2. 게임 상태 전송
            LoadGameMessage loadGame = new LoadGameMessage(game);
            session.getRemote().sendString(gson.toJson(loadGame));

            // 3. 연결된 사용자 정보
            String username = communicator.getUsername(authToken);
            String playerColor = getPlayerColor(gameID, username);
            String role = (playerColor != null) ? playerColor.toLowerCase() : "observer";
            String message = username + " connected as " + role;

            // 4. 알림 브로드캐스트
            communicator.broadcast(authToken, gameID, new NotificationMessage(message));
        } catch (Exception e) {
            sendError(session, "Error: " + e.getMessage());
        }
    }




    private String getPlayerColor(int gameID, String username) throws DataAccessException {
        var gameData = communicator.getGameDAO().getGame(gameID);
        if (username.equals(gameData.whiteUsername())) return "WHITE";
        if (username.equals(gameData.blackUsername())) return "BLACK";
        return null; // observer
    }


    private void handleMakeMove(String authToken, Integer gameID, String json) {
        // TODO: json → MakeMoveCommand로 재파싱 후 처리
    }

    private void handleLeave(String authToken, Integer gameID) {
        // TODO
    }

    private void handleResign(String authToken, Integer gameID) {
        // TODO
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
