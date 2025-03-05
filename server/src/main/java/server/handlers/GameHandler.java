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
                return handleErrorResponse(res, e);
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
                return handleErrorResponse(res, e);
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
                return handleErrorResponse(res, e);
            }
        };
    }

    /** ✅ 중복된 오류 응답 처리를 위한 메서드 */
    private String handleErrorResponse(Response res, DataAccessException e) {
        int statusCode = switch (e.getMessage()) {
            case "Error: unauthorized" -> 401;
            case "Error: already taken" -> 403;
            default -> 400;
        };
        res.status(statusCode);

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Error: " + e.getMessage());
        return gson.toJson(errorResponse);
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
