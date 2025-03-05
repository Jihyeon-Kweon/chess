package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    private static final AtomicInteger gameIDCounter = new AtomicInteger(1);

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public List<GameData> listGames(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return gameDAO.listGames();
    }

    public GameData createGame(String authToken, String gameName) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        if (gameName == null || gameName.isEmpty()) {
            throw new DataAccessException("Error: bad request");
        }

        int gameID = gameIDCounter.getAndIncrement();
        GameData game = new GameData(gameID, null, null, gameName, null);

        gameDAO.createGame(game);

        GameData savedGame = gameDAO.getGame(gameID);
        if (savedGame == null || savedGame.gameID() != gameID) {
            throw new DataAccessException("Error: failed to create game (invalid game ID)");
        }

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

        // 자리 체크
        if ("WHITE".equalsIgnoreCase(playerColor) && game.whiteUsername() != null ||
                "BLACK".equalsIgnoreCase(playerColor) && game.blackUsername() != null) {
            throw new DataAccessException("Error: already taken");
        }

        if ("WHITE".equalsIgnoreCase(playerColor)) {
            game = new GameData(game.gameID(), auth.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            game = new GameData(game.gameID(), game.whiteUsername(), auth.username(), game.gameName(), game.game());
        }

        gameDAO.updateGame(game);
    }
}