package websocket;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketCommunicator {

    private static final Map<String, Session> connections = new ConcurrentHashMap<>(); // authToken → Session
    private static final Map<String, String> tokenToUsername = new ConcurrentHashMap<>(); // authToken → username

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketCommunicator(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void addConnection(String authToken, Session session) {
        connections.put(authToken, session);
        try {
            AuthData auth = authDAO.getAuth(authToken);
            if (auth != null) {
                tokenToUsername.put(authToken, auth.username());
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    public void removeConnection(String authToken) {
        connections.remove(authToken);
        tokenToUsername.remove(authToken);
    }

    public void sendMessage(String authToken, ServerMessage message) {
        Session session = connections.get(authToken);
        if (session != null && session.isOpen()) {
            try {
                session.getRemote().sendString(gson.toJson(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcastToGame(int gameID, ServerMessage message, String exceptAuthToken) {
        for (var entry : connections.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();

            if (!session.isOpen()) continue;

            try {
                String username = tokenToUsername.get(token);
                if (username == null) continue;

                GameData game = gameDAO.getGame(gameID);
                if (game == null) continue;

                boolean inGame = username.equals(game.whiteUsername()) || username.equals(game.blackUsername());

                if (inGame && !token.equals(exceptAuthToken)) {
                    session.getRemote().sendString(gson.toJson(message));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void broadcast(String senderToken, int gameID, ServerMessage message) throws DataAccessException {
        for (var entry : connections.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();

            if (!session.isOpen()) continue;

            if (!token.equals(senderToken)) {
                sendMessage(session, message);
            }
        }
    }

    public void sendMessage(Session session, ServerMessage message) {
        try {
            System.out.println("🔔 Sending message to session: " + session);
            System.out.println("📡 Session is open: " + session.isOpen());
            System.out.println("📨 Message: " + gson.toJson(message));

            session.getRemote().sendString(gson.toJson(message));

            System.out.println("✅ Message successfully sent.");
        } catch (IOException e) {
            System.out.println("❌ Failed to send message:");
            e.printStackTrace();
        }
    }


    public String getUsername(String authToken) throws DataAccessException {
        String username = tokenToUsername.get(authToken);
        if (username == null) throw new DataAccessException("Error: invalid authToken");
        return username;
    }

    public String getAuthToken(String username) {
        for (Map.Entry<String, Session> entry : connections.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();

            if (!session.isOpen()) continue;

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
