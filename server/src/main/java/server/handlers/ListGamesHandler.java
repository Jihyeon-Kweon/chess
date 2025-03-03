package server.handlers;

import com.google.gson.Gson;
import service.ListGamesService;
import model.GameList;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.logging.ErrorManager;

public class ListGamesHandler implements Route{
    @Override
    public Object handle(Request req, Response res){
        Gson gson = new Gson();
        ListGamesService listGamesService = new ListGamesService();

        try{
            GameList gameList = listGamesService.getGames();
            res.status(200);
            return gson.toJson(gameList);
        } catch(Exception e){
            res.status(500);
            return gson.toJson(new ErrorMessage("Internal Server Error"));
        }
    }

    private static class ErrorMessage{
        String message;

        ErrorMessage(String message){
            this.message = message;
        }
    }

}
