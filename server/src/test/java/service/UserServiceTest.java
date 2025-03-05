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
    void testRegisterSuccess() throws DataAccessException {
        UserData user = new UserData("alice", "password123", "alice@example.com");
        AuthData auth = userService.register(user);

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("alice", auth.username());
    }

    @Test
    void testRegisterFailAlreadyExists() throws DataAccessException {
        UserData user1 = new UserData("bob", "securepass", "bob@example.com");
        userService.register(user1);

        UserData user2 = new UserData("bob", "newpass", "bob_new@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(user2));
    }

    @Test
    void testRegisterFailMissingUsername() {
        UserData user = new UserData(null, "password", "user@example.com");
        assertThrows(DataAccessException.class, () -> userService.register(user));
    }

    @Test
    void testLoginSuccess() throws DataAccessException {
        UserData user = new UserData("charlie", "mypassword", "charlie@example.com");
        userService.register(user);

        AuthData auth = userService.login("charlie", "mypassword");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("charlie", auth.username());
    }

    @Test
    void testLoginFailUserNotFound() {
        assertThrows(DataAccessException.class, () -> userService.login("nonexistent", "password"));
    }

    @Test
    void testLoginFailWrongPassword() throws DataAccessException {
        UserData user = new UserData("dave", "correctpass", "dave@example.com");
        userService.register(user);

        assertThrows(DataAccessException.class, () -> userService.login("dave", "wrongpass"));
    }

    @Test
    void testLogoutSuccess() throws DataAccessException {
        UserData user = new UserData("eve", "pass123", "eve@example.com");
        AuthData auth = userService.register(user);

        userService.logout(auth.authToken());
        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void testLogoutFailInvalidToken() {
        assertThrows(DataAccessException.class, () -> userService.logout("invalid_token"));
    }
}
