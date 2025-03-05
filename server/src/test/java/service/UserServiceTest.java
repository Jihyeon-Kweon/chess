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
        UserData user = new UserData("testUser", "password123", "test@email.com");
        AuthData auth = userService.register(user);

        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("testUser", auth.username());
    }

    @Test
    void testRegisterDuplicateUser() {
        UserData user = new UserData("duplicateUser", "password123", "test@email.com");

        assertDoesNotThrow(() -> userService.register(user)); // 첫 번째 등록 성공
        DataAccessException thrown = assertThrows(DataAccessException.class, () -> userService.register(user));

        assertEquals("Error: already taken", thrown.getMessage());
    }
}
