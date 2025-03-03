package server.handlers;

import com.google.gson.Gson;
import service.GameService;
import model.GameData;
import service.ListGamesService;
import spark.Request;
import spark.Response;
import spark.Route;

public class CreateGameHandler implements Route{
    @Override
    public Object handle(Request req, Response res){
        Gson gson = new Gson();
        GameService gameService = new GameService();

        GameData gameRequest = gson.fromJson(req.body(), GameData.class);
        GameData createdGame = gameService.createGame(gameRequest);

        res.status(200);
        return gson.toJson(createdGame);

    }
}
