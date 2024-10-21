package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class SQLiteUtils {

//    static Connection getConnection() throws SQLException {
//        return DriverManager.getConnection("jdbc:sqlite:C:/Users/Roman.Vatagin/Documents/LEARN/romarepo/webserver/src/main/resources/database/romarepo.db");
//    }

    Connection connect(String databaseLocation) {
        // SQLite connection string
        String url = "jdbc:sqlite:" + databaseLocation;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    void initDatabase(Connection conn) {
        // SQL statement for creating a new table
        String sql = "CREATE TABLE IF NOT EXISTS artifacts (\n"
                + " id integer PRIMARY KEY,\n"
                + " groupid text NOT NULL,\n"
                + " artifactid text NOT NULL \n"
                + " version text NOT NULL \n"
                + ");";

        try (conn;
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    void insert(String groupId, String artifactId, String version, Connection conn) {
        String sql = "INSERT INTO users(name, email) VALUES(?,?)";

        try (conn;
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, groupId);
            pstmt.setString(2, artifactId);
            pstmt.setString(2, version);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    void selectAll(Connection conn) {
        String sql = "SELECT groupId, artifactId, version FROM artifacts";

        try (conn;
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // Loop through the result set
            while (rs.next()) {
                System.out.println(rs.getString("groupId: ") + "\t" +
                        rs.getString("artifactId: ") + "\t" +
                        rs.getString("version: "));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
