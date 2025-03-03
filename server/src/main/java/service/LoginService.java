package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class LoginService {
    public AuthData login(String username, String password) throws DataAccessException{
        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();

        if (username == null || password == null || username.isBlank()||password.isBlank()){
            throw new DataAccessException("Error: bad request");
        }

        UserData user = userDAO.getUser(username);
        if (user == null||!user.getPassword().equals(password)){
            throw new DataAccessException("Error: unauthorized");
        }

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);

        return authData;
    }
}
