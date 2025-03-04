package server.handlers;

import com.google.gson.Gson;
import dataaccess.GameDAO;
import model.GameList;
import spark.Request;
import spark.Response;
import spark.Route;

public class ListGamesHandler implements Route {
    @Override
    public Object handle(Request req, Response res) {
        try {
            GameDAO gameDAO = new GameDAO();
            GameList gameList = gameDAO.getAllGames();

            res.status(200);
            return new Gson().toJson(gameList);
        } catch (Exception e) {
            res.status(500);
            return new Gson().toJson(new ErrorResponse("Error: Unable to retrieve games"));
        }
    }

    private static class ErrorResponse {
        String message;

        ErrorResponse(String message) {
            this.message = message;
        }
    }
}
