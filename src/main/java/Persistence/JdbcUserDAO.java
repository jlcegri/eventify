package Persistence;

import Model.Entities.User;
import Model.Services.UserDAO;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JdbcUserDAO implements UserDAO {

    @Override
    public void save(User user) {
        // 1. Handle name splitting
        String[] nameParts = splitName(user.getName());
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        // 2. Convert lists to CSV
        String interestsCSV = convertListToCSV(user.getInterests());
        String eventTypesCSV = convertListToCSV(user.getEventTypes());

        String sql = "INSERT INTO USERS (firstName, lastName, email, password, location, bio, interests_csv, eventTypes_csv, firstEntry) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getLocation());
            stmt.setString(6, user.getBio());
            stmt.setString(7, interestsCSV);
            stmt.setString(8, eventTypesCSV);
            stmt.setBoolean(9, user.isFirstEntry());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving user: " + user.getEmail(), e);
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM USERS WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by ID: " + id, e);
        }
        return null;
    }

    @Override
    public User findByEmail(String email) {
        String sql = "SELECT * FROM USERS WHERE email = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email: " + email, e);
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM USERS";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all users.", e);
        }
        return users;
    }

    @Override
    public boolean update(User user) {
        // 1. Handle name splitting
        String[] nameParts = splitName(user.getName());
        String firstName = nameParts[0];
        String lastName = nameParts[1];

        // 2. Convert lists to CSV
        String interestsCSV = convertListToCSV(user.getInterests());
        String eventTypesCSV = convertListToCSV(user.getEventTypes());

        String sql = "UPDATE USERS SET firstName = ?, lastName = ?, email = ?, password = ?, location = ?, bio = ?, interests_csv = ?, eventTypes_csv = ?, firstEntry = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getLocation());
            stmt.setString(6, user.getBio());
            stmt.setString(7, interestsCSV);
            stmt.setString(8, eventTypesCSV);
            stmt.setBoolean(9, user.isFirstEntry());
            stmt.setInt(10, user.getId());

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error updating user: " + user.getId(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM USERS WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Error deleting user with ID: " + id, e);
        }
    }

    @Override
    public User validateLogin(String email, String password) {
        String sql = "SELECT * FROM USERS WHERE email = ? AND password = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error during login validation.", e);
        }
        return null;
    }

    /**
     * @brief Combines firstName and lastName from the DB to create the 'name' field of the User object.
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");

        String firstName = rs.getString("firstName");
        String lastName = rs.getString("lastName");
        String name = firstName + (lastName != null && !lastName.isEmpty() ? " " + lastName : "");

        String email = rs.getString("email");
        String password = rs.getString("password");
        String location = rs.getString("location");
        String bio = rs.getString("bio");

        List<String> interests = convertCSVToList(rs.getString("interests_csv"));
        List<String> eventTypes = convertCSVToList(rs.getString("eventTypes_csv"));

        User user = new User(id, name, email, password, location, interests, eventTypes, bio);
        user.setFirstEntry(rs.getBoolean("firstEntry"));
        return user;
    }

    /**
     * @brief Splits the full name of the User object into firstName and lastName.
     * Assumes the first word is firstName and the rest is lastName.
     * @param fullName The full name from the User object.
     * @return A String array [firstName, lastName].
     */
    private String[] splitName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return new String[]{"", ""};
        }
        String[] parts = fullName.trim().split("\\s+", 2);
        String firstName = parts.length > 0 ? parts[0] : "";
        String lastName = parts.length > 1 ? parts[1] : "";
        return new String[]{firstName, lastName};
    }

    private List<String> convertCSVToList(String csv) {
        if (csv == null || csv.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private String convertListToCSV(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(", ", list);
    }
}