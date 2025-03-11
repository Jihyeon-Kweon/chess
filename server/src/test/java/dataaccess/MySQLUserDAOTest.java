package dataaccess;

import model.UserData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class MySQLUserDAOTest {
    private static MySQLUserDAO userDAO;

    @BeforeAll
    static void setup() {
        userDAO = new MySQLUserDAO();
    }

    @BeforeEach
    void clearBefore() throws DataAccessException {
        userDAO.clear();
    }

    @Test
    @DisplayName("insertUser - success")
    void testInsertUserSuccess() throws DataAccessException {
        UserData user = new UserData("testUser", "password123", "test@example.com");
        userDAO.insertUser(user);
        UserData retrievedUser = userDAO.getUser("testUser");
        assertNotNull(retrievedUser);
        assertEquals("testUser", retrievedUser.username());
    }

    @Test
    @DisplayName("insertUser - failed to add duplicate users")
    void testInsertUserFailDuplicate() throws DataAccessException {
        UserData user = new UserData("duplicateUser", "password123", "test@example.com");
        userDAO.insertUser(user);
        assertThrows(DataAccessException.class, () -> userDAO.insertUser(user));
    }

    @Test
    @DisplayName("getUser - lookup existing users")
    void testGetUserSuccess() throws DataAccessException {
        UserData user = new UserData("findMe", "securePass", "findme@example.com");
        userDAO.insertUser(user);
        UserData retrievedUser = userDAO.getUser("findMe");
        assertNotNull(retrievedUser);
    }

    @Test
    @DisplayName("getUser - lookup non-existing users")
    void testGetUserFailNotFound() throws DataAccessException {
        assertNull(userDAO.getUser("nonExistentUser"));
    }

    @Test
    @DisplayName("clear - delete all users")
    void testClearUsers() throws DataAccessException {
        userDAO.insertUser(new UserData("test1", "pass", "a@a.com"));
        userDAO.insertUser(new UserData("test2", "pass", "b@b.com"));
        userDAO.clear();
        assertNull(userDAO.getUser("test1"));
        assertNull(userDAO.getUser("test2"));
    }
}
