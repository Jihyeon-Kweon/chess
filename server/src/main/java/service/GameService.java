package service;

import dataaccess.GameDAO;
import model.GameData;

public class GameService {
    public GameData createGame(GameData gameRequest){
        GameDAO gamesDAO = new GameDAO();
        return gamesDAO.addGame(gameRequest.gameName(), gameRequest.whiteUsername(), gameRequest.blackUsername());
    }
}
