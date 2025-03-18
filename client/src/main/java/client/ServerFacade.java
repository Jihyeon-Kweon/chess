package client;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.*;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Sends a registration request to the server.
     *
     * @param username The username for registration.
     * @param password The password for registration.
     * @param email    The email for registration.
     * @return true if registration was successful, false otherwise.
     */
    public boolean registerUser(String username, String password, String email) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);
        requestBody.put("email", email);

        String body = new Gson().toJson(requestBody);
        String response = sendRequest(serverUrl + "/user", "POST", body);

        if (response == null) {
            return false;
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);

            if (jsonResponse.containsKey("error")) {
                System.out.println("Error: " + jsonResponse.get("error"));
                return false;
            }

            return true;
        } catch (Exception e) {
            return false; // 예외 발생 시 등록 실패 처리
        }
    }


    /**
     * Sends a login request to the server.
     *
     * @param username The username for login.
     * @param password The password for login.
     * @return true if login was successful, false otherwise.
     */
    public boolean loginUser(String username, String password) {
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("username", username);
        requestBody.put("password", password);

        String body = new Gson().toJson(requestBody);
        String response = sendRequest(serverUrl + "/session", "POST", body);

        if (response == null) {
            return false; // 로그인 실패
        }

        try {
            Map<String, Object> jsonResponse = new Gson().fromJson(response, Map.class);
            return jsonResponse.containsKey("authToken"); // 서버가 인증 토큰을 주면 성공
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * Sends an HTTP request to the specified server endpoint.
     *
     * @param url    The full URL of the endpoint.
     * @param method The HTTP method (e.g., GET, POST).
     * @param body   The JSON body of the request (optional, can be empty for GET requests).
     * @return The server response as a String, or null if an error occurs.
     */
    private String sendRequest(String url, String method, String body) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.addRequestProperty("Content-Type", "application/json");

            // Write request body if present
            if (!body.isEmpty()) {
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes());
                }
            }

            // Read response (either success or error)
            InputStream responseStream = (connection.getResponseCode() < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (Reader reader = new InputStreamReader(responseStream)) {
                // Parse JSON response into a Map
                Map<String, Object> jsonResponse = new Gson().fromJson(reader, Map.class);

                // Extract meaningful message or return entire JSON as a string
                return jsonResponse.containsKey("message") ? (String) jsonResponse.get("message") : new Gson().toJson(jsonResponse);
            }

        } catch (Exception e) {
            System.out.println("Error sending request: " + e.getMessage());
            return null;
        }
    }

}
