package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.Map;

public class AuthDAO {
    private static final Map<String, AuthData> authDB = new HashMap<>();

    public void createAuth(AuthData authData) throws DataAccessException {
        if (authDB.containsKey(authData.authToken())) {
            throw new DataAccessException("Error: Auth token already exists");
        }
        authDB.put(authData.authToken(), authData);
    }

    // 특정 인증 토큰 조회
    public AuthData getAuth(String authToken) {
        return authDB.get(authToken);
    }

    // 특정 인증 토큰 삭제
//    public void deleteAuth(String authToken) {
//        authDB.remove(authToken);
//    }

    public void clearAuthTokens() {
        authDB.clear();  // 모든 authToken 삭제
    }

    public boolean deleteAuth(String authToken){
        return authDB.remove(authToken) != null;
    }
}
