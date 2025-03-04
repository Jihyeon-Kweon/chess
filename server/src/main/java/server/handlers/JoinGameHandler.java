package server.handlers;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import model.JoinGameRequest;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.Objects;

public class JoinGameHandler implements Route {
    private final GameDAO gameDAO;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameDAO gameDAO) {
        this.gameDAO = gameDAO;
    }

    @Override
    public Object handle(Request req, Response res) {
        System.out.println("Received request: " + req.body());

        try {
            AuthDAO authDAO = new AuthDAO();
            String authToken = req.headers("authorization");
            AuthData Object = authDAO.getAuth(authToken);
            if(Object == null){
                res.status(401);
                return gson.toJson(new ErrorResponse("Error: unauthorized"));
            }

            JoinGameRequest joinRequest = gson.fromJson(req.body(), JoinGameRequest.class);
            System.out.println("Parsed request: " + joinRequest);
            if (!joinRequest.playerColor().equals("WHITE") && !joinRequest.playerColor().equals("BLACK")) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: bad request"));
            }



            GameData game = gameDAO.getGameById(joinRequest.gameID());
            System.out.println("Fetched game: " + game);

            if (game == null) {
                res.status(400);
                return gson.toJson(new ErrorResponse("Error: Game not found"));
            }
            ChessGame.TeamColor teamColor;
            if (Objects.equals(joinRequest.playerColor(), "WHITE")){
                teamColor = ChessGame.TeamColor.WHITE;
            } else{
                teamColor = ChessGame.TeamColor.BLACK;
            }

            GameData updatedGame = gameDAO.joinGame(joinRequest.gameID(), Object.username(), teamColor);

            if (updatedGame == null) {
                res.status(403);
                return gson.toJson(new ErrorResponse("Error: Could not join game"));
            }

            System.out.println("Updated game: " + updatedGame);
            res.status(200);
            return gson.toJson(updatedGame);

        } catch (Exception e) {
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
