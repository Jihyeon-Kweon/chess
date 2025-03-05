package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    void testCreateGameSuccess() throws DataAccessException {
        AuthData auth = new AuthData("validToken", "testUser");
        authDAO.createAuth(auth);

        GameData game = gameService.createGame("validToken", "Test Game");
        assertNotNull(game);
        assertEquals("Test Game", game.gameName());
    }

    @Test
    void testCreateGameUnauthorized() {
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> gameService.createGame("invalidToken", "Test Game"));
        assertEquals("Error: unauthorized", thrown.getMessage());
    }
}
