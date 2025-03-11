package dataaccess;

import model.GameData;
import chess.ChessGame;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {
    private static MySQLGameDAO gameDAO;

    @BeforeAll
    static void setup() {
        gameDAO = new MySQLGameDAO();
    }

    @BeforeEach
    void clearBefore() throws DataAccessException {
        gameDAO.clear();
    }

    @Test
    @DisplayName("✅ createGame - 성공 케이스")
    void testCreateGameSuccess() throws DataAccessException {
        // ✅ 먼저 유저 추가
        UserData whiteUser = new UserData("whitePlayer", "password123", "white@example.com");
        UserData blackUser = new UserData("blackPlayer", "password123", "black@example.com");
        userDAO.insertUser(whiteUser);
        userDAO.insertUser(blackUser);

        // ✅ 게임 추가
        GameData game = new GameData(1, "whitePlayer", "blackPlayer", "TestGame", "{}");  // 빈 JSON으로 초기화
        gameDAO.createGame(game);

        // ✅ 게임 조회 테스트
        GameData retrievedGame = gameDAO.getGame(1);
        assertNotNull(retrievedGame);
        assertEquals("whitePlayer", retrievedGame.whiteUsername());
        assertEquals("blackPlayer", retrievedGame.blackUsername());
    }


    @Test
    @DisplayName("❌ getGame - 존재하지 않는 게임 조회")
    void testGetGameFailNotFound() throws DataAccessException {
        assertNull(gameDAO.getGame(99999));
    }

    @Test
    @DisplayName("✅ updateGame - 게임 업데이트 성공")
    void testUpdateGameSuccess() throws DataAccessException {
        ChessGame gameState = new ChessGame();
        GameData game = new GameData(0, "player1", null, "Initial Game", gameState);
        int gameID = gameDAO.createGame(game);

        GameData updatedGame = new GameData(gameID, "player1", "player2", "Updated Game", gameState);
        gameDAO.updateGame(updatedGame);

        GameData retrievedGame = gameDAO.getGame(gameID);
        assertEquals("player2", retrievedGame.blackUsername());
        assertEquals("Updated Game", retrievedGame.gameName());
    }

    @Test
    @DisplayName("✅ clear - 모든 게임 삭제")
    void testClearGames() throws DataAccessException {
        gameDAO.createGame(new GameData(0, "p1", "p2", "Game1", new ChessGame()));
        gameDAO.createGame(new GameData(0, "p3", "p4", "Game2", new ChessGame()));
        gameDAO.clear();
        assertNull(gameDAO.getGame(1));
        assertNull(gameDAO.getGame(2));
    }
}
