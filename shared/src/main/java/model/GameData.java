package model;

import chess.ChessGame;

import com.google.gson.annotations.SerializedName;

public record GameData(
        Integer gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        ChessGame game
) {}

