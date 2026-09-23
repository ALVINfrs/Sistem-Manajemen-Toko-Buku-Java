package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.Retur;

public class ReturDAOImpl implements ReturDAO {

    private static final String BASE_SELECT =
            "SELECT r.id_retur, r.no_retur, r.tanggal, r.id_penjualan, p.no_nota, "
            + "r.id_buku, b.kode_buku, b.judul, r.qty, r.alasan, "
            + "COALESCE((SELECT dp.harga_jual FROM detail_penjualan dp WHERE dp.id_penjualan = r.id_penjualan AND dp.id_buku = r.id_buku LIMIT 1), b.harga_jual) AS harga_jual "
            + "FROM retur r "
            + "JOIN penjualan p ON r.id_penjualan = p.id_penjualan "
            + "JOIN buku b ON r.id_buku = b.id_buku";

    private Retur map(ResultSet rs) throws SQLException {
        Retur r = new Retur();
        r.setIdRetur(rs.getInt("id_retur"));
        r.setNoRetur(rs.getString("no_retur"));
        Timestamp ts = rs.getTimestamp("tanggal");
        r.setTanggal(ts == null ? null : ts.toLocalDateTime());
        r.setIdPenjualan(rs.getInt("id_penjualan"));
        r.setNoNota(rs.getString("no_nota"));
        r.setIdBuku(rs.getInt("id_buku"));
        r.setKodeBuku(rs.getString("kode_buku"));
        r.setJudul(rs.getString("judul"));
        r.setQty(rs.getInt("qty"));
        r.setAlasan(rs.getString("alasan"));
        r.setHargaJual(rs.getDouble("harga_jual"));
        return r;
    }

    @Override
    public boolean insert(Retur r) {
        String sql = "INSERT INTO retur (no_retur, tanggal, id_penjualan, id_buku, qty, alasan) "
                + "VALUES (?,?,?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getNoRetur());
            ps.setTimestamp(2, Timestamp.valueOf(r.getTanggal()));
            ps.setInt(3, r.getIdPenjualan());
            ps.setInt(4, r.getIdBuku());
            ps.setInt(5, r.getQty());
            ps.setString(6, r.getAlasan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert retur: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Retur r) {
        String sql = "UPDATE retur SET no_retur=?, tanggal=?, id_penjualan=?, id_buku=?, qty=?, alasan=? "
                + "WHERE id_retur=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, r.getNoRetur());
            ps.setTimestamp(2, Timestamp.valueOf(r.getTanggal()));
            ps.setInt(3, r.getIdPenjualan());
            ps.setInt(4, r.getIdBuku());
            ps.setInt(5, r.getQty());
            ps.setString(6, r.getAlasan());
            ps.setInt(7, r.getIdRetur());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update retur: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM retur WHERE id_retur=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus retur: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Retur> getAll() {
        String sql = BASE_SELECT + " ORDER BY r.id_retur DESC";
        List<Retur> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua retur: " + e.getMessage(), e);
        }
    }

    @Override
    public Retur getById(int id) {
        String sql = BASE_SELECT + " WHERE r.id_retur=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil retur by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Retur> search(String keyword) {
        String sql = BASE_SELECT + " WHERE (r.no_retur LIKE ? OR p.no_nota LIKE ? OR b.judul LIKE ? OR b.kode_buku LIKE ?) ORDER BY r.id_retur DESC";
        List<Retur> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            ps.setString(4, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal cari retur: " + e.getMessage(), e);
        }
    }

    @Override
    public int getReturQty(int idPenjualan, int idBuku) {
        String sql = "SELECT COALESCE(SUM(qty),0) FROM retur WHERE id_penjualan=? AND id_buku=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPenjualan);
            ps.setInt(2, idBuku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hitung qty retur: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean saveRetur(Retur r) {
        String sqlInsert = "INSERT INTO retur (no_retur, tanggal, id_penjualan, id_buku, qty, alasan) "
                + "VALUES (?,?,?,?,?,?)";
        String sqlStok = "UPDATE buku SET stok = stok + ? WHERE id_buku = ?";
        Connection c = null;
        try {
            c = Koneksi.getConnection();
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sqlInsert)) {
                ps.setString(1, r.getNoRetur());
                ps.setTimestamp(2, Timestamp.valueOf(r.getTanggal()));
                ps.setInt(3, r.getIdPenjualan());
                ps.setInt(4, r.getIdBuku());
                ps.setInt(5, r.getQty());
                ps.setString(6, r.getAlasan());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement(sqlStok)) {
                ps.setInt(1, r.getQty());
                ps.setInt(2, r.getIdBuku());
                ps.executeUpdate();
            }
            c.commit();
            return true;
        } catch (SQLException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Gagal simpan retur (rollback gagal): " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Gagal simpan retur: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Gagal simpan retur (rollback gagal): " + ex.getMessage(), ex);
                }
            }
            throw e;
        } finally {
            if (c != null) {
                try {
                    c.setAutoCommit(true);
                } catch (SQLException e) {
                    throw new RuntimeException("Gagal kembalikan auto-commit: " + e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<model.LapRetur> lapRetur(java.time.LocalDate dari, java.time.LocalDate sampai) {
        String sql = "SELECT r.no_retur, r.tanggal, p.no_nota, b.kode_buku, b.judul, r.qty, "
                + "COALESCE((SELECT dp.harga_jual FROM detail_penjualan dp WHERE dp.id_penjualan = r.id_penjualan AND dp.id_buku = r.id_buku LIMIT 1), b.harga_jual) AS harga_jual, "
                + "r.alasan "
                + "FROM retur r "
                + "JOIN penjualan p ON r.id_penjualan = p.id_penjualan "
                + "JOIN buku b ON r.id_buku = b.id_buku "
                + "WHERE DATE(r.tanggal) BETWEEN ? AND ? "
                + "ORDER BY r.tanggal DESC, r.id_retur DESC";
        List<model.LapRetur> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(dari));
            ps.setDate(2, java.sql.Date.valueOf(sampai));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("tanggal");
                    java.time.LocalDateTime tgl = ts == null ? null : ts.toLocalDateTime();
                    int qty = rs.getInt("qty");
                    double hargaJual = rs.getDouble("harga_jual");
                    double totalRefund = qty * hargaJual;
                    list.add(new model.LapRetur(
                            rs.getString("no_retur"),
                            tgl,
                            rs.getString("no_nota"),
                            rs.getString("kode_buku"),
                            rs.getString("judul"),
                            qty,
                            hargaJual,
                            totalRefund,
                            rs.getString("alasan")
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan retur: " + e.getMessage(), e);
        }
    }
}
