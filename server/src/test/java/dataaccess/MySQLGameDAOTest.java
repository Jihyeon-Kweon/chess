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


}
