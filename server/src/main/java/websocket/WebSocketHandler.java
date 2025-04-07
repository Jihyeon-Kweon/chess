package websocket;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
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
    private final Gson gson = new Gson();

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket connected: " + session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String messageJson) {
        try {
            // 메시지를 UserGameCommand로 파싱
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
            // 1. 연결 저장
            communicator.addConnection(authToken, session);

            // 2. 게임 데이터 가져오기
            var game = gameService.getGame(gameID, authToken);

            // 3. 현재 보드 상태 전송
            LoadGameMessage loadGame = new LoadGameMessage(game);
            session.getRemote().sendString(gson.toJson(loadGame));

            // 4. 연결된 유저 이름
            String username = communicator.getUsername(authToken);
            String playerColor = getPlayerColor(gameID, username);

            String role = (playerColor != null) ? playerColor.toLowerCase() : "observer";
            String message = username + " connected as " + role;

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
