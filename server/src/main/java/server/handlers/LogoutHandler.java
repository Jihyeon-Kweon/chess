package server.handlers;

import com.google.gson.Gson;
import org.junit.platform.commons.util.ReflectionUtils;
import service.LogoutService;
import spark.Request;
import spark.Response;
import spark.Route;

public class LogoutHandler implements Route{
    @Override
    public Object handle(Request req, Response res){
        Gson gson = new Gson();
        LogoutService logoutService = new LogoutService();

        String authToken = req.headers("Authorization");

        if (authToken == null){
            res.status(401);
            return gson.toJson(new ErrorMessage("Unauthorized: Missing auth token"));
        }
        boolean success = logoutService.logout(authToken);

        if(success){
            res.status(200);
            return gson.toJson(new SuccessMessage("Logout successful"));
        } else{
            res.status(401);
            return gson.toJson(new ErrorMessage("Unauthorized: Invalid auth token"));
        }
    }

    private static class ErrorMessage{
        String message;
        ErrorMessage(String message){this.message = "Error: " + message;}
    }

    private static class SuccessMessage{
        String message;
        SuccessMessage(String message){this.message=message;}
    }
}
