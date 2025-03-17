package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String register(String username, String password, String email) throws IOException {
        String endpoint = "/user"; // ✅ 회원가입 엔드포인트 수정
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password,
                "email", email
        );
        return sendPostRequest(endpoint, requestBody);
    }

    public String login(String username, String password) throws IOException {
        String endpoint = "/session"; // ✅ 로그인 엔드포인트 수정
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password
        );
        return sendPostRequest(endpoint, requestBody);
    }

    public void logout(String authToken) throws IOException{
        sendDeleteRequest("/session", authToken);
    }

    private void sendDeleteRequest(String endpoint, String authToken) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", authToken);
        conn.setDoOutput(true);
        conn.connect();
    }

    public String listGames(String authToken) throws IOException {
        String endpoint = "/game";
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", authToken); // 인증 토큰 추가

        int statusCode = conn.getResponseCode();
        try (InputStreamReader reader = new InputStreamReader(
                statusCode >= 200 && statusCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                "utf-8")) {
            BufferedReader br = new BufferedReader(reader);
            String response = br.readLine();

            if (statusCode >= 200 && statusCode < 300) {
                return response; // 정상 응답
            } else {
                throw new IOException("Server error: " + response); // 오류 응답
            }
        }
    }


    private String sendGetRequest(String endpoint, String authToken) throws IOException{
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", authToken);

        return readResponse(conn);
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int status = conn.getResponseCode();
        InputStream responseStream = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream, "utf-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }


    private String sendPostRequest(String endpoint, Map<String, String> body) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(body).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int statusCode = conn.getResponseCode();

        try (InputStreamReader reader = new InputStreamReader(
                statusCode >= 200 && statusCode < 300 ? conn.getInputStream() : conn.getErrorStream(),
                "utf-8")) {
            BufferedReader br = new BufferedReader(reader);
            String response = br.readLine();

            if (statusCode >= 200 && statusCode < 300) {
                return response; // 정상 응답
            } else {
                throw new IOException("Server error: " + response); // 오류 응답
            }
        }
    }

}