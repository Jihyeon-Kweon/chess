package model;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {

    private static final Gson GSON = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation() // <-- 이 줄 추가
            .setPrettyPrinting()
            .create();

    public String gameState() {
        return GSON.toJson(game); // ChessGame을 JSON으로 변환
    }

    public static ChessGame fromGameState(String gameStateJson) {
        Type type = new TypeToken<ChessGame>() {}.getType();
        return GSON.fromJson(gameStateJson, ChessGame.class); // JSON을 ChessGame 객체로 변환
    }
}
