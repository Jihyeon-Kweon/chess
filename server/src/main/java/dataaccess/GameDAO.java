package dataaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import model.GameData;
import model.GameList;

public class GameDAO {
    private static Map<Integer, String> games = new HashMap<>();

    public void clearGames(){
        games.clear();
    }

    public GameList getAllGames(){
        List<GameData> games = Database.getAllGames();
        return new GameList(games);
    }
}
