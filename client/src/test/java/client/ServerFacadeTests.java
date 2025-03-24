package client;

import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static int port;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clear() {
        facade.sendRequest("http://localhost:" + port + "/db", "DELETE", "", null);
    }

    // ✅ Register
    @Test
    public void testRegisterUser_Positive() {
        boolean result = facade.registerUser("user1", "pass", "user1@email.com");
        assertTrue(result);
    }

    @Test
    public void testRegisterUser_Negative() {
        facade.registerUser("user1", "pass", "user1@email.com");
        boolean result = facade.registerUser("user1", "diffpass", "other@email.com"); // 이미 존재하는 사용자
        assertFalse(result);
    }

    // ✅ Login
    @Test
    public void testLoginUser_Positive() {
        facade.registerUser("user2", "pass2", "user2@email.com");
        boolean result = facade.loginUser("user2", "pass2");
        assertTrue(result);
    }

    @Test
    public void testLoginUser_Negative() {
        boolean result = facade.loginUser("notExist", "wrongpass");
        assertFalse(result);
    }

    // ✅ Create Game
    @Test
    public void testCreateGame_Positive() {
        facade.registerUser("creator", "pass", "c@email.com");
        facade.loginUser("creator", "pass");
        boolean result = facade.createGame("My Game");
        assertTrue(result);
    }

    @Test
    public void testCreateGame_Negative_NotLoggedIn() {
        ServerFacade freshFacade = new ServerFacade("http://localhost:" + port);
        boolean result = freshFacade.createGame("NoAuth Game");
        assertFalse(result);
    }

    // ✅ List Games
    @Test
    public void testListGames_Positive() {
        facade.registerUser("lister", "pass", "l@email.com");
        facade.loginUser("lister", "pass");
        facade.createGame("Game A");
        List<GameData> games = facade.listGames();
        assertFalse(games.isEmpty());
    }

    @Test
    public void testListGames_Negative_NotLoggedIn() {
        ServerFacade freshFacade = new ServerFacade("http://localhost:" + port);
        List<GameData> games = freshFacade.listGames();
        assertTrue(games.isEmpty());
    }

    // ✅ Join Game
    @Test
    public void testJoinGame_Positive() {
        facade.registerUser("joiner", "pass", "j@email.com");
        facade.loginUser("joiner", "pass");
        facade.createGame("JoinMe");
        List<GameData> games = facade.listGames();
        int gameID = games.get(0).gameID();
        boolean result = facade.joinGame(gameID, "white");
        assertTrue(result);
    }

    @Test
    public void testJoinGame_Negative_InvalidGameID() {
        facade.registerUser("joiner2", "pass", "j2@email.com");
        facade.loginUser("joiner2", "pass");
        boolean result = facade.joinGame(99999, "black"); // 존재하지 않는 게임 ID
        assertFalse(result);
    }
}
