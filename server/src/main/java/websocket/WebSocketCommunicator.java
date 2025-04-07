package websocket;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
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
}
