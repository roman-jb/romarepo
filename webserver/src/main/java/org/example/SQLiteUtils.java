package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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
        String sql = "CREATE TABLE IF NOT EXISTS artifacts ("
                + " id integer PRIMARY KEY,"
                + " groupid text NOT NULL,"
                + " artifactid text NOT NULL,"
                + " version text NOT NULL"
                + ")";

        try (conn;
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    //TODO: Optimize bulk inserts! (Separate method?)
    void insert(MavenArtifact artifact, Connection conn) {
        String sql = "INSERT INTO artifacts(groupid, artifactid, version) VALUES('"
                + artifact.getGroupId() + "', '"
                + artifact.getArtifactId() + "', '"
                + artifact.getVersion() + "')";

        try (conn;
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
//            pstmt.setString(1, groupId);
//            pstmt.setString(2, artifactId);
//            pstmt.setString(2, version);
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

    List<MavenArtifact> findArtifacts(String searchString, Connection conn) {
        List<MavenArtifact> searchResults = new ArrayList<>();
        String sqliteQuery = "SELECT * FROM artifacts WHERE " +
                "groupId LIKE '%"+ searchString + "%'" +
                " OR artifactId LIKE '%"+ searchString + "%'";
        try (conn;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sqliteQuery)) {
            while (rs.next()) {
                MavenArtifact artifact = new MavenArtifact(rs.getString("groupId"),
                        rs.getString("artifactId"),
                        rs.getString("version"));
                searchResults.add(artifact);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return searchResults;
    }
}






































