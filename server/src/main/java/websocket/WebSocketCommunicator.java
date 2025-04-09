package websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketCommunicator {

    // 전체 연결 (optional: 디버깅 or fallback용)
    private static final Map<String, Session> CONNECTIONS = new ConcurrentHashMap<>(); // authToken → Session

    // 게임별 연결 관리: gameID → (authToken → Session)
    private static final Map<Integer, Map<String, Session>> GAME_CONNECTIONS = new ConcurrentHashMap<>();

    // authToken → username 매핑
    private static final Map<String, String> TOKEN_TO_USERNAME = new ConcurrentHashMap<>();

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketCommunicator(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void addConnection(String authToken, int gameID, Session session) {
        CONNECTIONS.put(authToken, session);
        GAME_CONNECTIONS.computeIfAbsent(gameID, k -> new ConcurrentHashMap<>()).put(authToken, session);

        try {
            AuthData auth = authDAO.getAuth(authToken);
            if (auth != null) {
                TOKEN_TO_USERNAME.put(authToken, auth.username());
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public void removeConnection(String authToken) {
        CONNECTIONS.remove(authToken);
        TOKEN_TO_USERNAME.remove(authToken);

        // 게임 세션에서도 제거
        for (Map<String, Session> gameMap : GAME_CONNECTIONS.values()) {
            gameMap.remove(authToken);
        }
    }

    public void sendMessage(String authToken, ServerMessage message) {
        Session session = CONNECTIONS.get(authToken);
        if (session != null && session.isOpen()) {
            sendMessage(session, message);
        }
    }

    public void sendMessage(Session session, ServerMessage message) {
        try {
            System.out.println("Sending message to session: " + session);
            System.out.println("Session is open: " + session.isOpen());
            System.out.println("Message: " + gson.toJson(message));

            session.getRemote().sendString(gson.toJson(message));

            System.out.println("Message successfully sent.");
        } catch (IOException e) {
            System.out.println("Failed to send message:");
            e.printStackTrace();
        }
    }

    public void broadcastToGame(int gameID, ServerMessage message, String exceptAuthToken) {
        Map<String, Session> gameSessions = GAME_CONNECTIONS.get(gameID);
        if (gameSessions == null) {
            return;
        }

        for (Map.Entry<String, Session> entry : gameSessions.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();

            if (!session.isOpen()) {
                continue;
            }

            if (!token.equals(exceptAuthToken)) {
                sendMessage(session, message);
            }
        }
    }

    public String getUsername(String authToken) throws DataAccessException {
        String username = TOKEN_TO_USERNAME.get(authToken);
        if (username == null) {
            throw new DataAccessException("Error: invalid authToken");
        }
        return username;
    }

    public String getAuthToken(String username) {
        for (Map.Entry<String, Session> entry : CONNECTIONS.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();

            if (!session.isOpen()) {
                continue;
            }

            try {
                AuthData auth = authDAO.getAuth(token);
                if (auth != null && auth.username().equals(username)) {
                    return token;
                }
            } catch (DataAccessException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public GameDAO getGameDAO() {
        return this.gameDAO;
    }

    public AuthDAO getAuthDAO() {
        return this.authDAO;
    }
}
