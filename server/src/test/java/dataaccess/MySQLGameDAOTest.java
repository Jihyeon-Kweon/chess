package dataaccess;

import model.GameData;
import model.UserData;
import chess.ChessGame;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MySQLGameDAOTest {
    private static MySQLGameDAO gameDAO;
    private static MySQLUserDAO userDAO;

    @BeforeAll
    static void setup() throws DataAccessException {  // 예외 처리 필요!
        gameDAO = new MySQLGameDAO();
        userDAO = new MySQLUserDAO();

        // ✅ users 테이블 초기화 후 사용자 삽입
        userDAO.clear();
        userDAO.insertUser(new UserData("whitePlayer", "password", "white@example.com"));
        userDAO.insertUser(new UserData("blackPlayer", "password", "black@example.com"));
    }


    @BeforeEach
    void clearBefore() throws DataAccessException {
        gameDAO.clear();
        userDAO.clear();
        userDAO.insertUser(new UserData("whitePlayer", "password", "white@example.com"));
        userDAO.insertUser(new UserData("blackPlayer", "password", "black@example.com"));
    }


    @Test
    @DisplayName("✅ createGame - 게임 생성 성공")
    void testCreateGameSuccess() throws DataAccessException {
        ChessGame game = new ChessGame();
        GameData gameData = new GameData(0, "whitePlayer", "blackPlayer", "Test Game", game);

        int gameID = gameDAO.createGame(gameData);
        assertTrue(gameID > 0);
    }


    @Test
    @DisplayName("❌ createGame - NULL 데이터로 생성 실패")
    void testCreateGameFail() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.createGame(null);
        });
        assertEquals("GameData cannot be null", exception.getMessage());
    }


    @Test
    @DisplayName("❌ getGame - 존재하지 않는 게임 ID 조회")
    void testGetGameFail() throws DataAccessException {
        assertNull(gameDAO.getGame(999999));
    }




    @Test
    @DisplayName("clear - delete all games")
    void testClearGames() throws DataAccessException {
        // ✅ 게임 추가 전에 사용자 등록
        userDAO.insertUser(new UserData("white1", "password", "white1@example.com"));
        userDAO.insertUser(new UserData("black1", "password", "black1@example.com"));
        userDAO.insertUser(new UserData("white2", "password", "white2@example.com"));
        userDAO.insertUser(new UserData("black2", "password", "black2@example.com"));

        gameDAO.createGame(new GameData(0, "white1", "black1", "Game1", new ChessGame()));
        gameDAO.createGame(new GameData(0, "white2", "black2", "Game2", new ChessGame()));

        gameDAO.clear();
        assertTrue(gameDAO.listGames().isEmpty());
    }
}

