package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;

public class ClearService {
    public void clear() throws DataAccessException{
        UserDAO userDAO = new UserDAO();
        GameDAO gameDAO = new GameDAO();
        AuthDAO authDAO = new AuthDAO();

        try{
            userDAO.clearUsers();
            gameDAO.clearGames();
            authDAO.clearAuthTokens();
        } catch (Exception e){
            throw new DataAccessException("Error clearing database: " + e.getMessage());
        }
    }
}
