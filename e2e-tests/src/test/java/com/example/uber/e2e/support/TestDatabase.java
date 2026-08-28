package com.example.uber.e2e.support;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class TestDatabase {

    private TestDatabase() {
    }

    public static int findUnratedFinishedRideId(String passengerEmail) {
        String sql =
                """
                SELECT r.id
                FROM rides r
                JOIN users u ON u.id = r.passenger_id
                WHERE u.email = ?
                  AND r.status = 'FINISHED'
                  AND NOT EXISTS (SELECT 1 FROM ratings ra WHERE ra.ride_id = r.id)
                ORDER BY r.id
                LIMIT 1
                """;
        return queryRideId(sql, passengerEmail, null);
    }

    public static int findRideIdByStatus(String passengerEmail, String status) {
        String sql =
                """
                SELECT r.id
                FROM rides r
                JOIN users u ON u.id = r.passenger_id
                WHERE u.email = ? AND r.status = ?::ride_status
                ORDER BY r.id
                LIMIT 1
                """;
        return queryRideId(sql, passengerEmail, status);
    }

    private static int queryRideId(String sql, String passengerEmail, String status) {
        try (Connection connection =
                        DriverManager.getConnection(TestConfig.DB_URL, TestConfig.DB_USER, TestConfig.DB_PASSWORD);
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, passengerEmail);
            if (status != null) {
                statement.setString(2, status);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException(
                            "No matching ride fixture found for " + passengerEmail
                                    + " — check db/init/02-seed.sql");
                }
                return resultSet.getInt("id");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to query the test database", e);
        }
    }
}
