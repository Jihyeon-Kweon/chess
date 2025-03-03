package dataaccess;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import model.GameData;
import model.GameList;
import chess.ChessGame;
import java.util.ArrayList;

public class GameDAO {
    private static Map<Integer, GameData> games = new HashMap<>();
    private static int nextId = 1;

    public void clearGames(){
        games.clear();
        nextId = 1;
    }

    public GameList getAllGames(){
        return new GameList(new ArrayList<>(games.values()));
    }

    public GameData addGame(String gameName, String whiteUsername, String blackUsername) {
        GameData newGame = new GameData(nextId, whiteUsername, blackUsername, gameName, new ChessGame());         games.put(nextId, newGame);
        nextId++;
        return newGame;
    }
}
