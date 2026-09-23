package util;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import koneksi.Koneksi;
public class NotaGenerator {
    public static synchronized String next(String prefix, String table, String column) throws SQLException {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String sql = "SELECT " + column + " FROM " + table + " WHERE " + column + " LIKE ? ORDER BY " + column + " DESC LIMIT 1";
        // GUARD: table/column hanya dari 3 konstanta internal berikut - bukan input user:
        // ("NJ","penjualan","no_nota"), ("FB","pembelian","no_faktur"), ("RT","retur","no_retur").
        if (!isAllowed(prefix, table, column)) throw new IllegalArgumentException("Kombinasi nota tidak dikenal");
        try (Connection c = Koneksi.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, prefix + "-" + date + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                int seq = 1;
                if (rs.next()) {
                    String last = rs.getString(1);
                    seq = Integer.parseInt(last.substring(last.lastIndexOf('-') + 1)) + 1;
                }
                return String.format("%s-%s-%04d", prefix, date, seq);
            }
        }
    }
    private static boolean isAllowed(String p, String t, String c) {
        return ("NJ".equals(p) && "penjualan".equals(t) && "no_nota".equals(c))
            || ("FB".equals(p) && "pembelian".equals(t) && "no_faktur".equals(c))
            || ("RT".equals(p) && "retur".equals(t) && "no_retur".equals(c));
    }
}
