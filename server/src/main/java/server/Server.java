package server;

import server.handlers.*;
import spark.*;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/clear", new ClearHandler());
        Spark.delete("/db", new ClearHandler());

        Spark.post("/user", new RegisterHandler());
        Spark.post("/user/login", new LoginHandler());
        Spark.post("/session", new LoginHandler());

        Spark.delete("/session", new LogoutHandler());

        Spark.get("/games", new ListGamesHandler());

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
