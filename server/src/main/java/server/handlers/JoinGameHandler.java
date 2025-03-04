package server.handlers;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinGameRequest;
import spark.Request;
import spark.Response;
import spark.Route;

public class JoinGameHandler implements Route {
    @Override
    public Object handle(Request req, Response res) {
        Gson gson = new Gson();
        System.out.println("Received request: "+req.body());

        try {
            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            System.out.println("Parsed request: " + joinRequest);

            GameDAO gameDAO = new GameDAO();
            GameData game = gameDAO.getGameById(joinRequest.gameId());
            System.out.println("Fetched game: "+game);

            if (game == null) {
                res.status(404);
                return gson.toJson(new ErrorResponse("Error: Game not found"));
            }

            try {
                GameData updatedGame = gameDAO.joinGame(joinRequest.gameId(), joinRequest.username());
                System.out.println("Updated game: "+updatedGame);

                res.status(200);
                return gson.toJson(updatedGame);
            } catch (IllegalStateException e) {
                res.status(403);
                return gson.toJson(new ErrorResponse(e.getMessage()));
            }

        } catch (Exception e) {
            // JSON 파싱 실패 또는 기타 오류 발생 시 400 에러 반환
            res.status(400);
            return gson.toJson(new ErrorResponse("Error: Invalid request"));
        }
    }

    private static class ErrorResponse {
        String message;

        ErrorResponse(String message) {
            this.message = message;
        }
    }
}
