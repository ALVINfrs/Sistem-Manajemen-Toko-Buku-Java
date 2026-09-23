package koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {
    private static final String URL = "jdbc:mysql://localhost:3306/db_toko_buku?serverTimezone=Asia/Jakarta";
    private static final String USER = "root";
    private static final String PASS = "";
    private static Connection conn;

    private Koneksi() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(URL, USER, PASS);
        }
        return conn;
    }

    public static void main(String[] args) {
        try {
            getConnection();
            System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}
