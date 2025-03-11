package dataaccess;

import model.GameData;
import model.UserData;
import chess.ChessGame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MySQLGameDAOTest {

    private MySQLGameDAO gameDAO;
    private MySQLUserDAO userDAO;

    @BeforeEach
    void clearDatabase() throws DataAccessException {
        gameDAO = new MySQLGameDAO();
        userDAO = new MySQLUserDAO();

        // 데이터베이스 초기화
        gameDAO.clear();
        userDAO.clear(); // MySQLUserDAO에 clear() 메서드가 있어야 함.

        // 테스트용 사용자 추가
        userDAO.insertUser(new UserData("whitePlayer", "password", "white@example.com"));
        userDAO.insertUser(new UserData("blackPlayer", "password", "black@example.com"));
    }


    @Test
    void testGetGameNotFound() throws DataAccessException {
        GameData retrievedGame = gameDAO.getGame(999);
        assertNull(retrievedGame, "Game should not be found for invalid ID");
    }



    @Test
    void testClearGames() throws DataAccessException {
        ChessGame chessGame = new ChessGame();
        gameDAO.createGame(new GameData(0, "whitePlayer", "blackPlayer", "Test Game", chessGame));

        gameDAO.clear();
        List<GameData> games = gameDAO.listGames();
        assertEquals(0, games.size(), "All games should be cleared from the database");
    }
}

