package server.handlers;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import model.GameData;
import model.JoinGameRequest;
import spark.Request;
import spark.Response;
import spark.Route;
import chess.ChessGame;

public class JoinGameHandler implements Route {
    private final GameDAO gameDAO;
    private final Gson gson;

    public JoinGameHandler(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
        this.gson = new Gson();
    }

    @Override
    public Object handle(Request req, Response res) {
        System.out.println("Received request: " + req.body());

        try {
            // 요청 JSON 파싱
            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            System.out.println("Parsed request: " + joinRequest);

            // gameId 확인
            GameData game = gameDAO.getGameById(joinRequest.gameId());
            System.out.println("Fetched game: " + game);

            if (game == null) {
                res.status(404);
                return gson.toJson(new ErrorResponse("Error: Game not found"));
            }

            ChessGame.TeamColor teamColor = joinRequest.teamColor();
            if (teamColor == null) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: Team color must be specified"));
            }

            try {
                GameData updatedGame = gameDAO.joinGame(joinRequest.gameId(), joinRequest.username(), teamColor);
                System.out.println("Updated game: " + updatedGame);

                res.status(200);
                return gson.toJson(updatedGame);
            } catch (IllegalStateException e) {
                res.status(403);
                return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
            }

        } catch (Exception e) {
            // JSON 파싱 실패 또는 기타 오류 발생 시 400 에러 반환
            res.status(400);
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
