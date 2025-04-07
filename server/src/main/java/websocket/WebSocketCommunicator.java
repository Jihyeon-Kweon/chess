package websocket;

import com.google.gson.Gson;
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
    private final Gson gson = new Gson();
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public WebSocketCommunicator(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public void addConnection(String authToken, Session session) {
        connections.put(authToken, session);
    }

    public void removeConnection(String authToken) {
        connections.remove(authToken);
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
                AuthData auth = authDAO.getAuth(token);
                if (auth == null) continue;

                GameData game = gameDAO.getGame(gameID);
                if (game == null) continue;

                String username = auth.username();
                boolean inGame = username.equals(game.whiteUsername()) ||
                        username.equals(game.blackUsername());

                if (inGame && !token.equals(exceptAuthToken)) {
                    session.getRemote().sendString(gson.toJson(message));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getUsername(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: invalid authToken");
        return auth.username();
    }

    public void broadcast(String senderToken, int gameID, ServerMessage message) throws DataAccessException {
        GameData game = gameDAO.getGame(gameID);

        for (var entry : connections.entrySet()) {
            String token = entry.getKey();
            Session session = entry.getValue();

            if (!token.equals(senderToken)) {
                String username = getUsername(token);

                boolean isInGame = username.equals(game.whiteUsername()) ||
                        username.equals(game.blackUsername());

                if (isInGame || message instanceof NotificationMessage) {
                    sendMessage(session, message);
                }
            }
        }
    }

    public GameDAO getGameDAO() {
        return this.gameDAO;
    }

    private void sendMessage(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
