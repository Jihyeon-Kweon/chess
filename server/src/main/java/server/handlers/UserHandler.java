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
                int statusCode = e.getMessage().contains("already taken") ? 403 : 400;
                return handleErrorResponse(res, statusCode, e.getMessage());
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
                return handleErrorResponse(res, 401, e.getMessage());
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
                return handleErrorResponse(res, 401, e.getMessage());
            }
        };
    }

    /** 공통 오류 응답 처리 메서드 */
    private String handleErrorResponse(Response res, int statusCode, String errorMessage) {
        res.status(statusCode);
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("message", "Error: " + errorMessage);
        return gson.toJson(errorResponse);
    }

    private static class ResponseObject {}
}
