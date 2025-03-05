package server;

import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import service.ClearService;
import service.GameService;
import service.UserService;
import server.handlers.ClearHandler;
import server.handlers.GameHandler;
import server.handlers.UserHandler;
import spark.Spark;

public class Server {
    public int run(int desiredPort) {
        Spark.port(desiredPort);
        Spark.staticFiles.location("web");

        // DAO 객체 생성
        var userDAO = new MemoryUserDAO();
        var gameDAO = new MemoryGameDAO();
        var authDAO = new MemoryAuthDAO();

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

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}
