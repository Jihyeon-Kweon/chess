package client;

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.GameData;

public class ServerFacade {
    private final String serverUrl;
    private String authToken;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Sends a registration request to the server.
     */
    public boolean registerUser(String username, String password, String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);

        String body = new Gson().toJson(requestBody);
        String response = sendRequest(serverUrl + "/user", "POST", body, new HashMap<>());

        if (response == null) {
            return false;
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);

            // ✅ 여기 조건 수정: message가 "Error: ..."로 시작하면 실패
            if (jsonResponse.containsKey("message")) {
                String message = (String) jsonResponse.get("message");
                if (message.startsWith("Error")) {
                    System.out.println(message);  // 로그로 출력해도 좋음
                    return false;
                }
            }

            return true; // 성공
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Sends a login request to the server.
     */
    public boolean loginUser(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        String body = new Gson().toJson(requestBody);
        String response = sendRequest(serverUrl + "/session", "POST", body, new HashMap<>());

        if (response == null) {
            return false;
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
            if (jsonResponse.containsKey("authToken")) {
                this.authToken = (String) jsonResponse.get("authToken");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Sends a request to create a new game.
     */
    public boolean createGame(String gameName) {
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Error: User is not logged in.");
            return false;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("gameName", gameName);

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);

        String body = new Gson().toJson(requestBody);
        String response = sendRequest(serverUrl + "/game", "POST", body, headers);

        if (response == null) {
            return false;
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
            return !jsonResponse.containsKey("error");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Lists all available games from the server.
     */
    public List<GameData> listGames() {
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Error: User is not logged in.");
            return new ArrayList<>();
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);

        String response = sendRequest(serverUrl + "/game", "GET", "", headers);
//        System.out.println("Server response: " + response);

        if (response == null) {
            return new ArrayList<>();
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);

            if (jsonResponse == null || !jsonResponse.containsKey("games")) {
                System.out.println("Error: 'games' key missing in response.");
                return new ArrayList<>();
            }

            return new Gson().fromJson(new Gson().toJson(jsonResponse.get("games")),
                    new TypeToken<List<GameData>>() {}.getType());
        } catch (Exception e) {
            System.out.println("Error parsing game list response: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Sends a request to join a game.
     */
    public boolean joinGame(int gameID, String playerColor) {
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Error: User is not logged in.");
            return false;
        }

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("gameID", String.valueOf(gameID)); // int → String 변환
        requestBody.put("playerColor", playerColor.toUpperCase()); // 대소문자 정리

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);

        String body = new Gson().toJson(requestBody);
        String response = sendRequest(serverUrl + "/game", "PUT", body, headers); // 서버로 PUT 요청

        if (response == null) {
            return false;
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
            if (jsonResponse.containsKey("message") && jsonResponse.get("message").equals("Joined game successfully")) {
                return true;
            } else {
                System.out.println("Error: " + jsonResponse.get("message"));
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error parsing joinGame response: " + e.getMessage());
            return false;
        }
    }


    /**
     * Sends an HTTP request to the specified server endpoint.
     */
    protected String sendRequest(String url, String method, String body, Map<String, String> headers) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", "application/json");

            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    connection.setRequestProperty(entry.getKey(), entry.getValue());
                }
            }

            // 요청 본문이 있을 경우 전송
            if (!body.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes());
                }
            }

            // 응답 읽기
            InputStream responseStream = (connection.getResponseCode() < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }

        } catch (Exception e) {
            System.out.println("Error sending request: " + e.getMessage());
            return null;
        }
    }

    public boolean logout() {
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Error: User is not logged in.");
            return false;
        }

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);

        String response = sendRequest(serverUrl + "/session", "DELETE", "", headers);

        if (response == null) {
            return false;
        }

        // 로그아웃 성공 시 authToken 제거
        this.authToken = null;
        return true;
    }

    public boolean observeGame(int gameID) {
        if (authToken == null || authToken.isEmpty()) {
            System.out.println("Error: User is not logged in.");
            return false;
        }

        String url = serverUrl + "/game/" + gameID;

        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", authToken);

        String response = sendRequest(url, "GET", "", headers);
        System.out.println("ObserveGame Raw Response: " + response); // 🔍 추가된 디버그 로그

        if (response == null) {
            return false;
        }

        try {
            // 응답이 JSON 객체 형식인지 먼저 검사
            if (response.trim().startsWith("{")) {
                Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
                return jsonResponse.containsKey("game");
            } else {
                // 단순 문자열 응답이라면 에러 메시지로 출력
                System.out.println("Error: " + response);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error parsing observeGame response: " + e.getMessage());
            return false;
        }
    }

    public String getAuthToken() {
        return this.authToken;
    }



}