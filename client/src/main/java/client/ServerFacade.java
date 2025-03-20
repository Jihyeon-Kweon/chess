package client;

import java.io.*;
import java.net.*;
import java.util.*;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import model.GameData;

public class ServerFacade {
    private final String serverUrl;
    private String authToken; // ✅ 로그인 후 자동 저장

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

        if (response == null) return false;

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
            return !jsonResponse.containsKey("error");
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

        if (response == null) return false;

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
            if (jsonResponse.containsKey("authToken")) {
                this.authToken = (String) jsonResponse.get("authToken"); // ✅ 로그인 성공 시 저장
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

        if (response == null) return false;

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
        System.out.println("Server response: " + response);

        if (response == null) return new ArrayList<>();

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
    private String sendRequest(String url, String method, String body, Map<String, String> headers) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", "application/json");

            // ✅ 추가적인 헤더 설정
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
}