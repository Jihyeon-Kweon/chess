package dataaccess;

import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class UserDAO {
    private static final Map<String, UserData> userDB = new HashMap<>();

    public void createUser(UserData user) throws DataAccessException {
        if (userDB.containsKey(user.username())) {
            throw new DataAccessException("Error: User already exists");
        }
        userDB.put(user.username(), user);
    }

    public UserData getUser(String username) {
        return userDB.get(username);  // 없으면 null 반환
    }

    public void clearUsers() {
        userDB.clear();
    }
}
