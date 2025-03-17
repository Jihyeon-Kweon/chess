package client;

import com.google.gson.Gson;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String register(String username, String password, String email) throws IOException {
        String endpoint = "/register";
        Map<String, String> requestBody = Map.of(
                "username", username,
                "password", password,
                "email", email
        );

        return sendPostRequest(endpoint, requestBody);
    }

    public String login(String username, String password) throws IOException {
        String endpoint = "/login";
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
            byte[] input = gson.toJson(body).getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
            return br.readLine();
        }
    }
}
