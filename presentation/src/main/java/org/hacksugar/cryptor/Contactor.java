package org.hacksugar.cryptor;

import java.sql.*;

public class Contactor {
    private Connection databaseConnection;

    Contactor() {
        connect();
    }

    public void connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            conn = DriverManager.getConnection(
                    "jdbc:mysql://18.217.149.1:1521/hashes", "root",
                    "i be tard"
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
        return toSend.executeQuery();
    }

    public String getPublicKey(String phonenum) throws SQLException {
        String[] tmp = new String[] {
                phonenum
        };
        ResultSet rs = executeStatement("Select * FROM hashes WHERE phone= ?", tmp);
        return rs.getString("pub_hash");
    }
    public ResultSet addNewInfo(String phonenum, String pubkey) throws SQLException {
        String[] tmp = new String[] {
                phonenum, pubkey
        };
        return executeStatement("INSERT INTO hashes (phone, pub_hash) VALUES (\"?\", \"?\")", tmp);
    }

    public Connection getDatabaseConnection() {
        return this.databaseConnection;
    }
}
