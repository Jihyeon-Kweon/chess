package server.handlers;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import service.UserService;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

public class UserHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Route register() {
        return (Request req, Response res) -> {
            try {
                UserData user = gson.fromJson(req.body(), UserData.class);
                AuthData auth = userService.register(user);

                res.status(200);
                return gson.toJson(auth);
            } catch (DataAccessException e) {
                res.status(400);

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error: " + e.getMessage());
                return gson.toJson(errorResponse);
            }
        };
    }

    public Route login() {
        return (Request req, Response res) -> {
            try {
                UserData user = gson.fromJson(req.body(), UserData.class);
                AuthData auth = userService.login(user.username(), user.password());

                res.status(200);
                return gson.toJson(auth);
            } catch (DataAccessException e) {
                res.status(401);

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error: " + e.getMessage());
                return gson.toJson(errorResponse);
            }
        };
    }

    public Route logout() {
        return (Request req, Response res) -> {
            try {
                String authToken = req.headers("authorization");
                userService.logout(authToken);

                res.status(200);
                return gson.toJson(new ResponseObject());
            } catch (DataAccessException e) {
                res.status(401);

                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("message", "Error: " + e.getMessage());
                return gson.toJson(errorResponse);
            }
        };
    }

    private static class ResponseObject {}
}
