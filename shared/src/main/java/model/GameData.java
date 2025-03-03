package model;

import chess.ChessGame;

public record GameData(int id, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
