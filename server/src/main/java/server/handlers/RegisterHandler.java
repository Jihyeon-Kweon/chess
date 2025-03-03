package server.handlers;

import com.google.gson.Gson;
import service.RegisterService;
import dataaccess.DataAccessException;
import model.AuthData;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class RegisterHandler implements Route{
    @Override
    public Object handle(Request req, Response res){
        Gson gson = new Gson();
        RegisterService registerService = new RegisterService();

        try{
            Map<String, String> requestData = gson.fromJson(req.body(), Map.class);
            String username = requestData.get("username");
            String password = requestData.get("password");
            String email = requestData.get("email");

            AuthData authData = registerService.register(username, password, email);

            res.status(200);
            Map<String, String> responseData = new HashMap<>();
            responseData.put("username", authData.username());
            responseData.put("authToken", authData.authToken());
            return gson.toJson(responseData);
        } catch (DataAccessException e){
            if (e.getMessage().contains("bad request")){
                res.status(400);
            } else if (e.getMessage().contains("already taken")){
                res.status(403);
            } else{
                res.status(500);
            }
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return gson.toJson(error);
        }
    }
}
