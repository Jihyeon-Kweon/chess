package dataaccess;

import java.util.HashMap;
import java.util.Map;
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

    public GameData addGame(String gameName) {
        GameData newGame = new GameData(nextId, null, null, gameName, new ChessGame());
        games.put(nextId, newGame);
        nextId++;
        return newGame;
    }

    public GameData getGameById(int gameId){
        return games.get(gameId);
    }

    public GameData joinGame(int gameId, String username, ChessGame.TeamColor teamColor) {
        GameData game = games.get(gameId);
        if (game == null) {
            throw new IllegalStateException("Error: Game not found");
        }

        boolean alreadyWhite = username.equals(game.whiteUsername());
        boolean alreadyBlack = username.equals(game.blackUsername());

        if (alreadyWhite || alreadyBlack) {
            throw new IllegalStateException("Error: Player is already in the game");
        }

        String newWhite = game.whiteUsername();
        String newBlack = game.blackUsername();

        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (newWhite == null || newWhite.isEmpty()) {
                newWhite = username;
            } else {
                throw new IllegalStateException("Error: White position already taken");
            }
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            if (newBlack == null || newBlack.isEmpty()) {
                newBlack = username;
            } else {
                throw new IllegalStateException("Error: Black position already taken");
            }
        } else {
            throw new IllegalStateException("Error: Invalid team color");
        }

        GameData updatedGame = new GameData(game.id(), newWhite, newBlack, game.gameName(), game.game());
        games.put(gameId, updatedGame);
        return updatedGame;
    }
}
