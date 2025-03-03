package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class RegisterService {
    public AuthData register(String username, String password, String email)
        throws DataAccessException{
        UserDAO userDAO = new UserDAO();
        AuthDAO authDAO = new AuthDAO();

        if (username == null || password == null || email == null || username.isBlank() || password.isBlank() || email.isBlank()){
            throw new DataAccessException("Error: bad request");
        }

        if (userDAO.getUser(username)!= null){
            throw new DataAccessException("Error");
        }

        UserData newUser = new UserData(username, password, email);
        userDAO.createUser(newUser);

        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, username);
        authDAO.createAuth(authData);

        return authData;
    }
}
