package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {
    private UserService userService;
    private UserDAO userDAO;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void testRegister_Success() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        AuthData auth = userService.register(user);

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("alice", auth.username());
    }

    @Test
    void testRegister_Fail_AlreadyExists() throws DataAccessException {
        UserData user1 = new UserData("bob", "securepass", "bob@example.com");
        userService.register(user1);

        UserData user2 = new UserData("bob", "newpass", "bob_new@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(user2));
    }

    @Test
    void testRegister_Fail_MissingUsername() {
        UserData user = new UserData(null, "password", "user@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(user));
    }

    @Test
    void testLogin_Success() throws DataAccessException {
        UserData user = new UserData("charlie", "mypassword", "charlie@example.com");
        userService.register(user);

        AuthData auth = userService.login("charlie", "mypassword");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("charlie", auth.username());
    }

    @Test
    void testLogin_Fail_UserNotFound() {
        assertThrows(DataAccessException.class, () -> userService.login("nonexistent", "password"));
    }

    @Test
    void testLogin_Fail_WrongPassword() throws DataAccessException {
        UserData user = new UserData("dave", "correctpass", "dave@example.com");
        userService.register(user);

        assertThrows(DataAccessException.class, () -> userService.login("dave", "wrongpass"));
    }

    @Test
    void testLogout_Success() throws DataAccessException {
        UserData user = new UserData("eve", "pass123", "eve@example.com");
        AuthData auth = userService.register(user);

        userService.logout(auth.authToken());
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void testLogout_Fail_InvalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("invalid_token"));
    }
}
