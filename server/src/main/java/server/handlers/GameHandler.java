package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }


    public Route listGames() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                List<GameData> games = gameService.listGames(authToken);

                res.status(200);

                Map<String, Object> response = new HashMap<>();
                response.put("games", games);
                return gson.toJson(response);
            } catch (DataAccessException e) {
                res.status(401);

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error: " + e.getMessage());
                return gson.toJson(errorResponse);
            }
        };
    }

    public Route createGame() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                GameRequest requestData = gson.fromJson(req.body(), GameRequest.class);
                GameData game = gameService.createGame(authToken, requestData.gameName);

                res.status(200);
                return gson.toJson(game);
            } catch (DataAccessException e) {
                res.status(e.getMessage().contains("unauthorized") ? 401 : 400);

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error: " + e.getMessage());
                return gson.toJson(errorResponse);
            }
        };
    }

    public Route joinGame() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                JoinGameRequest requestData = gson.fromJson(req.body(), JoinGameRequest.class);
                gameService.joinGame(authToken, requestData.gameID(), requestData.playerColor());

                res.status(200);
                return gson.toJson(new ResponseObject());
            } catch (DataAccessException e) {
                res.status(e.getMessage().contains("unauthorized") ? 401 :
                        e.getMessage().contains("already taken") ? 403 : 400);

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error: " + e.getMessage());
                return gson.toJson(errorResponse);
            }
        };
    }

    // JoinGame 요청 객체
    private static class JoinGameRequest {
        private String playerColor;
        private int gameID;

        public String playerColor() {
            return playerColor;
        }

        public int gameID() {
            return gameID;
        }
    }

    // CreateGame 요청 객체
    private static class GameRequest {
        private String gameName;
    }

    // ✅ 빈 JSON 응답을 위한 객체
    private static class ResponseObject {}
}
