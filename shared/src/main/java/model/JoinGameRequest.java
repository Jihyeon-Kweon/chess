package model;

import chess.ChessGame;

public record JoinGameRequest(int gameId, String username, ChessGame.TeamColor teamColor) {}
