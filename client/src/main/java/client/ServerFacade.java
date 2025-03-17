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

    private String sendPostRequest(String endpoint, Map<String, String> body) throws IOException {
        URL url = new URL(serverUrl + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = gson.toJson(body).getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = conn.getResponseCode();
        InputStream inputStream = (responseCode >= 200 && responseCode < 300) ? conn.getInputStream() : conn.getErrorStream();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }
}
