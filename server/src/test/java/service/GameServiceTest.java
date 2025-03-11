package service;

import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {
    private GameService gameService;
    private UserService userService;
    private GameDAO gameDAO;
    private AuthDAO authDAO;
    private UserDAO userDAO;
    private String authToken;

    @BeforeEach
    void setUp() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        authDAO = new MySQLAuthDAO();
        userDAO = new MySQLUserDAO();

        gameService = new GameService(gameDAO, authDAO);
        userService = new UserService(userDAO, authDAO);

        gameDAO.clear();
        authDAO.clear();
        userDAO.clear();

        // 유저 등록 후 인증 토큰 발급
        UserData user = new UserData("player1", "chess123", "player1@example.com");
        authToken = userService.register(user).authToken();
    }

    /** ✅ 성공 케이스: 게임 리스트 조회 */
    @Test
    void testListGamesSuccess() throws DataAccessException {
        List<GameData> games = gameService.listGames(authToken);
        assertNotNull(games);
    }

    /** ❌ 실패 케이스: 잘못된 authToken */
    @Test
    void testListGamesFailInvalidAuthToken() {
        assertThrows(DataAccessException.class, () -> gameService.listGames("invalid_token"));
    }

    /** ✅ 성공 케이스: 게임 생성 */
    @Test
    void testCreateGameSuccess() throws DataAccessException {
        GameData game = gameService.createGame(authToken, "Chess Match");
        assertNotNull(game);
        assertEquals("Chess Match", game.gameName());
    }

    /** ❌ 실패 케이스: 게임 이름 없이 생성 */
    @Test
    void testCreateGameFailNoName() {
        assertThrows(DataAccessException.class, () -> gameService.createGame(authToken, ""));
    }

    @Test
    void testCreateGameFailInvalidAuthToken() {
        assertThrows(DataAccessException.class, () -> gameService.createGame("invalid_token", "Test Game"));
    }

    @Test
    void testJoinGameSuccess() throws DataAccessException {
        GameData game = gameService.createGame(authToken, "Chess Game");
        gameService.joinGame(authToken, game.gameID(), "WHITE");

        GameData updatedGame = gameDAO.getGame(game.gameID());
        assertEquals("player1", updatedGame.whiteUsername());
    }

    @Test
    void testJoinGameFailInvalidGame() {
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, 99999, "WHITE"));
    }

    @Test
    void testJoinGameFailInvalidColor() throws DataAccessException {
        GameData game = gameService.createGame(authToken, "Chess Game");
        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, game.gameID(), "GREEN"));
    }

    @Test
    void testJoinGameFailAlreadyTaken() throws DataAccessException {
        GameData game = gameService.createGame(authToken, "Chess Game");
        gameService.joinGame(authToken, game.gameID(), "WHITE");

        assertThrows(DataAccessException.class, () -> gameService.joinGame(authToken, game.gameID(), "WHITE"));
    }
}
