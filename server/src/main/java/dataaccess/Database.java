package dataaccess;

import model.GameData;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private static List<GameData> games = new ArrayList<>();

    public static List<GameData> getAllGames(){
        return new ArrayList<>(games);
    }

    public static void addGame(GameData game){
        games.add(game);
    }

    public static void clearGames(){
        games.clear();
    }
}
