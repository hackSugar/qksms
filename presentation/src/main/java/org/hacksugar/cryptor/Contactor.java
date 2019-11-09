package org.hacksugar.cryptor;

import java.sql.*;

public class Contactor {
    private Connection databaseConnection;

    Contactor() {

    }

    public void connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(
                    "jdbc:oracle:thin:@18.217.149.1:1521:orcl", "query",
                    "blinkOS is like Windows!"
            );
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        this.databaseConnection = conn;
    }

    public ResultSet executeStatement(String statement, String[] args) throws SQLException {
        Connection conn = this.databaseConnection;
        PreparedStatement toSend = conn.prepareStatement(statement);
        for (int i = 0; i < args.length; i++) {
            toSend.setString(i + 1, args[i]);
        }
        toSend.executeUpdate();
        ResultSet rs = toSend.executeQuery();
        return rs;
    }

    public String getPublicKey(String phonenum) throws SQLException {
        String[] tmp = new String[] {
                phonenum
        };
        ResultSet rs = executeStatement("Select * FROM hashes WHERE phone=?", tmp);
        return rs.getString("pub_hash");

    }
}
