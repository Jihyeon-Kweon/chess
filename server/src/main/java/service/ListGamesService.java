package service;

import model.GameList;
import dataaccess.GameDAO;

public class ListGamesService {
    public GameList getGames(){
        GameDAO gameDAO = new GameDAO();
        return gameDAO.getAllGames();
    }
}
