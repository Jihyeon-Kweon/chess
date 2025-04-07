package server;

import dataaccess.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import spark.Spark;
import websocket.WebSocketHandler;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        DatabaseManager.initializeDatabase();

        // DAO 객체 생성
        var userDAO = new MySQLUserDAO();
        var gameDAO = new MySQLGameDAO();
        var authDAO = new MySQLAuthDAO();

        // Service 객체 생성
        var clearService = new ClearService(userDAO, gameDAO, authDAO);
        var userService = new UserService(userDAO, authDAO);
        var gameService = new GameService(gameDAO, authDAO);

        // Handler 객체 생성
        var clearHandler = new ClearHandler(clearService);
        var userHandler = new UserHandler(userService);
        var gameHandler = new GameHandler(gameService);

        // endpoint
        Spark.delete("/db", clearHandler);
        Spark.post("/user", userHandler.register());
        Spark.post("/session", userHandler.login());
        Spark.delete("/session", userHandler.logout());
        Spark.get("/game", gameHandler.listGames());
        Spark.post("/game", gameHandler.createGame());
        Spark.put("/game", gameHandler.joinGame());
        Spark.get("/game/:gameID", gameHandler.observeGame());

        // register websocket endpoint
        Spark.webSocket("/ws", WebSocketHandler.class);


        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}