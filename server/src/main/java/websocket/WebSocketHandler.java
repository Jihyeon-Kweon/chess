package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class WebSocketHandler {

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
        // TODO: 유효한 사용자/게임인지 검증
        // 세션 등록
        userSessions.put(authToken, session);

        // 게임 상태 불러오기 → LoadGameMessage 보내기
        // GameService와 WebSocketCommunicator 사용 예정
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
