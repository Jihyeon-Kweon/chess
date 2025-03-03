package server;

import spark.*;
import server.handlers.ClearHandler;
import server.handlers.RegisterHandler;
import server.handlers.ClearHandler;
import server.handlers.LoginHandler;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", new ClearHandler());

        Spark.post("/user", new RegisterHandler());

        Spark.post("/user/login", new LoginHandler());
        Spark.delete("/clear", new ClearHandler());


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
