package server.handlers;

import com.google.gson.Gson;
import service.LoginService;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.xml.crypto.Data;

public class LoginHandler implements Route{
    @Override
    public Object handle(Request req, Response res){
        Gson gson = new Gson();
        LoginService loginService = new LoginService();

        try{
            UserData loginRequest = gson.fromJson(req.body(), UserData.class);

            AuthData authData = loginService.login(loginRequest.getUsername(), loginRequest.getPassword());

            res.status(200);
            return gson.toJson(authData);
        } catch (DataAccessException e){
            if (e.getMessage().contains("bad request")){
                res.status(400);
            } else{
                res.status(401);
            }
            return gson.toJson(new ErrorMessage(e.getMessage()));
        }

    }

    private static class ErrorMessage{
        String message;

        ErrorMessage(String message){
            this.message = message;
        }
    }
}
