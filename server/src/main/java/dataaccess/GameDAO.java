package dataaccess;

import java.util.HashMap;
import java.util.Map;

public class GameDAO {
    private static Map<Integer, String> games = new HashMap<>();

    public void clearGames(){
        games.clear();
    }
}
