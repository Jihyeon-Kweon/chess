package dataaccess;

import java.util.HashMap;
import java.util.Map;
import model.GameData;
import model.GameList;
import chess.ChessGame;
import java.util.ArrayList;

public class GameDAO {
    private static int nextId = 1;
    private static Map<Integer, GameData> games = new HashMap<>();

    public void clearGames(){
        games.clear();
        nextId = 1;
    }

    public GameList getAllGames(){
        return new GameList(new ArrayList<>(games.values()));
    }

    public GameData addGame(String gameName) {
        return addGame(gameName, null, null);
    }

    public GameData addGame(String gameName, String whiteUsername, String blackUsername) {
        int gameId = nextId;
        GameData newGame = new GameData(gameId, whiteUsername, blackUsername, gameName, new ChessGame());

        System.out.println("Game Created: " + newGame);
        System.out.println("Game ID in addGame(): "+newGame.gameID());

        games.put(gameId, newGame);
        nextId++;
        return newGame;
    }



    public GameData getGameById(int gameId){
        return games.get(gameId);
    }

    public GameData joinGame(int gameId, String username, ChessGame.TeamColor teamColor) {
        GameData game = games.get(gameId);
        if (game == null) {
            return null;
        }

        boolean alreadyWhite = game.whiteUsername()!=null;
        boolean alreadyBlack = game.blackUsername()!=null;

        if (alreadyWhite || alreadyBlack) {
            return null;
        }

        String newWhite = game.whiteUsername();
        String newBlack = game.blackUsername();

        if (teamColor == ChessGame.TeamColor.WHITE) {
            if (newWhite == null || newWhite.isEmpty()) {
                newWhite = username;
            } else {
                return null; // WHITE 자리 이미 찼으면 참가 불가능
            }
        } else if (teamColor == ChessGame.TeamColor.BLACK) {
            if (newBlack == null || newBlack.isEmpty()) {
                newBlack = username;
            } else {
                return null; // BLACK 자리 이미 찼으면 참가 불가능
            }
        } else {
            return null;
        }

        GameData updatedGame = new GameData(game.gameID(), newWhite, newBlack, game.gameName(), game.game());
        games.put(gameId, updatedGame);
        return updatedGame;
    }

}
