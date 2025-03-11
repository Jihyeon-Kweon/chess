package model;

import chess.ChessGame;
import com.google.gson.Gson;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    public String gameState() {
        return new Gson().toJson(game);
    }

    public static ChessGame fromGameState(String gameStateJson) {
        return new Gson().fromJson(gameStateJson, ChessGame.class);
    }

}
