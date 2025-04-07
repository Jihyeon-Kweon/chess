package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        // ✅ games가 null이 아닐지 확인
        List<GameData> games = gameDAO.listGames();
        System.out.println("Retrieved games: " + games);

        if (games == null) {
            return new ArrayList<>(); // null 방지
        }
        return games;
    }


    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        GameData game = new GameData(0, null, null, gameName, null);
        int gameID = gameDAO.createGame(game);
        GameData savedGame = new GameData(gameID, null, null, gameName, null);
        return savedGame;
    }

    public void joinGame(String authToken, int gameID, String playerColor) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        if (!"WHITE".equalsIgnoreCase(playerColor) && !"BLACK".equalsIgnoreCase(playerColor)) {
            throw new DataAccessException("Error: bad request");
        }


        // **WHITE 자리 체크**
        if ("WHITE".equalsIgnoreCase(playerColor)) {
            if (game.whiteUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
        }

        // **BLACK 자리 체크**
        else if ("BLACK".equalsIgnoreCase(playerColor)) {
            if (game.blackUsername() != null) {
                throw new DataAccessException("Error: already taken");
            }
            game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
        }

        gameDAO.updateGame(game);
    }

    public GameData observeGame(String authToken, int gameID) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }

        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: bad request");
        }

        return game;
    }

    public ChessGame getGame(int gameID, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData game = gameDAO.getGame(gameID);
        if (game == null) throw new DataAccessException("Error: bad request");

        return game.game();
    }

    public ChessGame makeMove(int gameID, String authToken, ChessMove move) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) throw new DataAccessException("Error: bad request");

        ChessGame game = gameData.game();
        if (game == null) throw new DataAccessException("Error: no game state");

        if (game.isGameOver()) {
            throw new DataAccessException("Error: game is already over");
        }

        String username = auth.username();
        ChessGame.TeamColor playerColor;
        if (username.equals(gameData.whiteUsername())) {
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (username.equals(gameData.blackUsername())) {
            playerColor = ChessGame.TeamColor.BLACK;
        } else {
            throw new DataAccessException("Error: only players can move");
        }

        if (game.getTeamTurn() != playerColor) {
            throw new DataAccessException("Error: not your turn");
        }

        var validMoves = game.validMoves(move.getStartPosition());
        if (validMoves == null || !validMoves.contains(move)) {
            throw new DataAccessException("Error: invalid move");
        }

        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new DataAccessException("Error: invalid move");
        }

        gameDAO.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        ));

        return game;
    }


    public void resign(int gameID, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData gameData = gameDAO.getGame(gameID);
        if (gameData == null) throw new DataAccessException("Error: bad request");

        String username = auth.username();
        if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
            throw new DataAccessException("Error: only players can resign");
        }

        ChessGame game = gameData.game();
        game.setGameOver(true);  // 이 함수가 없다면 boolean 필드 추가해야 함

        gameDAO.updateGame(new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        ));
    }


    public void leaveGame(int gameID, String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) throw new DataAccessException("Error: unauthorized");

        GameData game = gameDAO.getGame(gameID);
        if (game == null) throw new DataAccessException("Error: bad request");

        String username = auth.username();
        GameData updated = game;

        if (username.equals(game.whiteUsername())) {
            updated = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
        } else if (username.equals(game.blackUsername())) {
            updated = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
        }

        gameDAO.updateGame(updated);
    }




}