package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MySQLAuthDAOTest {
    private static MySQLAuthDAO authDAO;
    private static MySQLUserDAO userDAO;

    @BeforeAll
    static void setup() {
        authDAO = new MySQLAuthDAO();
        userDAO = new MySQLUserDAO();
    }

    @BeforeEach
    void clearBefore() throws DataAccessException {
        authDAO.clear();
        userDAO.clear();  // ✅ users 테이블도 초기화
    }

    @Test
    @DisplayName("✅ createAuth - 성공 케이스")
    void testCreateAuthSuccess() throws DataAccessException {
        userDAO.insertUser(new UserData("testUser", "password123", "test@example.com")); // ✅ 먼저 유저 추가

        AuthData auth = new AuthData("validToken", "testUser");
        authDAO.createAuth(auth);

        AuthData retrievedAuth = authDAO.getAuth("validToken");
        assertNotNull(retrievedAuth);
        assertEquals("testUser", retrievedAuth.username());
    }

    @Test
    @DisplayName("❌ getAuth - 존재하지 않는 토큰 조회")
    void testGetAuthFailNotFound() throws DataAccessException {
        assertNull(authDAO.getAuth("invalidToken"));
    }

    @Test
    @DisplayName("✅ deleteAuth - 토큰 삭제 후 조회 실패")
    void testDeleteAuth() throws DataAccessException {
        userDAO.insertUser(new UserData("testUser", "password123", "test@example.com"));  // ✅ 유저 추가

        AuthData auth = new AuthData("deleteToken", "testUser");
        authDAO.createAuth(auth);
        authDAO.deleteAuth("deleteToken");

        assertNull(authDAO.getAuth("deleteToken"));
    }

    @Test
    @DisplayName("✅ clear - 모든 토큰 삭제")
    void testClearAuths() throws DataAccessException {
        userDAO.insertUser(new UserData("user1", "password123", "user1@example.com"));  // ✅ 유저 추가
        userDAO.insertUser(new UserData("user2", "password123", "user2@example.com"));

        authDAO.createAuth(new AuthData("token1", "user1"));
        authDAO.createAuth(new AuthData("token2", "user2"));

        authDAO.clear();
        assertNull(authDAO.getAuth("token1"));
        assertNull(authDAO.getAuth("token2"));
    }
}
