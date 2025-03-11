package dataaccess;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseManager {
    private static final String DB_PROPERTIES = "/db.properties";
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_NAME;

    static {
        try (InputStream input = DatabaseManager.class.getResourceAsStream(DB_PROPERTIES)) {
            Properties prop = new Properties();
            if (input == null) {
                throw new IOException("Unable to find db.properties");
            }
            prop.load(input);

            DB_URL = "jdbc:mysql://" + prop.getProperty("db.host") + ":" + prop.getProperty("db.port") + "/";
            DB_NAME = prop.getProperty("db.name");
            DB_USER = prop.getProperty("db.user");
            DB_PASSWORD = prop.getProperty("db.password");
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load database properties: " + ex.getMessage());
        }
    }

    /** DB 연결 가져오기 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL + DB_NAME, DB_USER, DB_PASSWORD);
    }

    /** DB 및 테이블 자동 생성 */
    public static void initializeDatabase() {
        createDatabase(); // 1️⃣ 데이터베이스 생성
        createTables();   // 2️⃣ 테이블 생성
    }

    /** 데이터베이스 생성 (없으면 생성) */
    private static void createDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement()) {

            String createDatabaseSQL = "CREATE DATABASE IF NOT EXISTS " + DB_NAME;
            stmt.executeUpdate(createDatabaseSQL);
            System.out.println("Database checked/created: " + DB_NAME);

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error creating database: " + e.getMessage());
        }
    }

    /** 테이블 생성 */
    private static void createTables() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // users 테이블 생성
            String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    username VARCHAR(50) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL,
                    email VARCHAR(100) NOT NULL
                )
            """;
            stmt.executeUpdate(createUsersTable);

            // games 테이블 생성
            String createGamesTable = """
                CREATE TABLE IF NOT EXISTS games (
                    gameID INT AUTO_INCREMENT PRIMARY KEY,
                    whiteUsername VARCHAR(50),
                    blackUsername VARCHAR(50),
                    gameName VARCHAR(100) NOT NULL,
                    gameState TEXT DEFAULT NULL,
                    FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                    FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
                )
            """;
            stmt.executeUpdate(createGamesTable);

            // auth_tokens 테이블 생성
            String createAuthTokensTable = """
                CREATE TABLE IF NOT EXISTS auth_tokens (
                    authToken VARCHAR(255) PRIMARY KEY,
                    username VARCHAR(50) NOT NULL,
                    FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
                )
            """;
            stmt.executeUpdate(createAuthTokensTable);

            System.out.println("All tables checked/created!");

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing database: " + e.getMessage());
        }
    }
}