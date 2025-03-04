package server.handlers;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import model.AuthData;
import service.GameService;
import model.GameCreateRequest;
import model.GameData;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route {
    private final GameService gameService = new GameService();
    private final Gson gson = new Gson();

    @Override
    public Object handle(Request req, Response res) {

        AuthDAO authDAO = new AuthDAO();
        String authToken = req.headers("authorization");
        AuthData Object = authDAO.getAuth(authToken);
        if(Object == null){
            res.status(401);
            return gson.toJson(new ErrorResponse("Error: unauthorized"));
        }

        try {
            GameCreateRequest gameRequest = gson.fromJson(req.body(), GameCreateRequest.class);
            GameData createdGame = gameService.createGame(gameRequest);

            res.status(200);
            return gson.toJson(createdGame);

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
