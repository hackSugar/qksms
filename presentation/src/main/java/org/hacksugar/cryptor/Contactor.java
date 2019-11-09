package org.hacksugar.cryptor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public static boolean checkEncrypt(List<String> numbers) {
        List<String> newNums = new ArrayList<>();
        for(String number: numbers) {
            if (number.contains("+")) {
                newNums.add(number.replace("+", ""));
            }
        }
        //TODO QUERY HERE AND RETURN IF ALL ARE ENCRYPTED-SUPPORTED
        return false;
    }

    public Connection getDatabaseConnection() {
        return this.databaseConnection;
    }
}
