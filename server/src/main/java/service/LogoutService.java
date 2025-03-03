package service;

import dataaccess.AuthDAO;

public class LogoutService {
    private final AuthDAO authDAO;

    public LogoutService(){
        this.authDAO = new AuthDAO();
    }

    public boolean logout(String authToken){
        return authDAO.deleteAuth(authToken);
    }
}
