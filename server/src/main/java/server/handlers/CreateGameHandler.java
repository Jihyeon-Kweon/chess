package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import model.GameData;
import model.GameCreateRequest;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
    @Override
    public Object handle(Request req, Response res) {
        Gson gson = new Gson();
        GameService gameService = new GameService();

        try {
            // JSON 요청을 GameCreateRequest 객체로 변환
            GameCreateRequest createRequest = gson.fromJson(req.body(), GameCreateRequest.class);

            if (createRequest == null || createRequest.gameName() == null || createRequest.gameName().isEmpty()) {
                res.status(400); // Bad Request
                return gson.toJson(new ErrorResponse("Error: Game name is required"));
            }

            // 새로운 게임 생성
            GameData createdGame = gameService.createGame(createRequest);

            res.status(201); // Created
            return gson.toJson(createdGame);

        } catch (Exception e) {
            res.status(400); // Bad Request
            return gson.toJson(new ErrorResponse("Error: Invalid request format"));
        }
    }

    private static class ErrorResponse {
        String message;
        ErrorResponse(String message) {
            this.message = message;
        }
    }
}
