package service;

import dataaccess.GameDAO;
import model.GameData;
import model.GameCreateRequest;

public class GameService {
    private final GameDAO gameDAO = new GameDAO();

    public GameData createGame(GameCreateRequest createRequest) {
        return gameDAO.addGame(createRequest.gameName());
    }
}
