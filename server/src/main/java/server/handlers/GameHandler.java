package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.GameData;
import service.GameService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    /** ✅ 게임 리스트 반환 */
    public Route listGames() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                System.out.println("Auth Token received: " + authToken); // ✅ 디버깅용 출력

                if (authToken == null || authToken.isEmpty()) {
                    res.status(401);
                    return gson.toJson(Map.of("message", "Error: unauthorized"));
                }

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



    /** ✅ 게임 생성 */
    public Route createGame() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                GameRequest requestData = gson.fromJson(req.body(), GameRequest.class);

                // 요청 데이터가 올바른지 확인
                if (requestData == null || requestData.gameName == null || requestData.gameName.trim().isEmpty()) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: game name is required"));
                }

                GameData game = gameService.createGame(authToken, requestData.gameName);

                res.status(200);
                return gson.toJson(Map.of(
                        "message", "Game created successfully",
                        "gameID", game.gameID(),
                        "gameName", game.gameName()
                ));
            } catch (DataAccessException e) {
                return handleErrorResponse(res, e);
            }
        };
    }

    /** ✅ 게임 참가 */
    public Route joinGame() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                JoinGameRequest requestData = gson.fromJson(req.body(), JoinGameRequest.class);

                // 요청 데이터 유효성 검사
                if (requestData == null || requestData.gameID() <= 0 || requestData.playerColor() == null) {
                    res.status(400);
                    return gson.toJson(Map.of("message", "Error: invalid request data"));
                }

                gameService.joinGame(authToken, requestData.gameID(), requestData.playerColor());

                res.status(200);
                return gson.toJson(Map.of("message", "Joined game successfully"));
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
            case "Error: game name already exists" -> 409;  // **중복 게임 이름은 409 Conflict 응답**
            default -> 400;
        };
        res.status(statusCode);

        return gson.toJson(Map.of("message", e.getMessage()));
    }

    /** ✅ 게임 생성 요청 객체 */
    private static class GameRequest {
        private String gameName;
    }

    /** ✅ 게임 참가 요청 객체 */
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
}
