package dataaccess;

import model.GameData;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MySQLGameDAO implements GameDAO {

    @Override
    public int createGame(GameData game) throws DataAccessException {
        String sql = "INSERT INTO games (game_name, white_username, black_username, state) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, game.gameName());
            stmt.setString(2, game.whiteUsername());
            stmt.setString(3, game.blackUsername());
            stmt.setString(4, "{}");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error creating game: " + e.getMessage());
        }
        throw new DataAccessException("Error: Game ID not generated.");
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = "SELECT id, game_name, white_username, black_username, state FROM games WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, gameID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new GameData(
                            rs.getInt("id"),
                            rs.getString("white_username"),
                            rs.getString("black_username"),
                            rs.getString("game_name"),
                            null
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error retrieving game: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> games = new ArrayList<>();
        String sql = "SELECT id, game_name, white_username, black_username, state FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("id"),
                        rs.getString("white_username"),
                        rs.getString("black_username"),
                        rs.getString("game_name"),
                        null
                ));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error listing games: " + e.getMessage());
        }
        return games;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = "UPDATE games SET white_username = ?, black_username = ?, state = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, game.whiteUsername());
            stmt.setString(2, game.blackUsername());
            stmt.setString(3, "{}");
            stmt.setInt(4, game.gameID());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error updating game: " + e.getMessage());
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String sql = "DELETE FROM games";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Error clearing games: " + e.getMessage());
        }
    }
}