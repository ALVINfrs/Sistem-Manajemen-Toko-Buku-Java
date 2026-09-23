package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.DetailPenjualan;
import model.LapGrafik;
import model.LapPendapatan;
import model.LapPenjualan;
import model.LapTerlaris;
import model.Penjualan;

public class PenjualanDAOImpl implements PenjualanDAO {

    private static final String BASE_SELECT =
            "SELECT p.id_penjualan, p.no_nota, p.tanggal, p.id_user, u.username, "
            + "p.id_member, m.kode_member, p.total, p.bayar, p.kembalian, "
            + "p.metode_bayar, p.diskon "
            + "FROM penjualan p "
            + "JOIN users u ON p.id_user = u.id_user "
            + "LEFT JOIN member m ON p.id_member = m.id_member";

    private Penjualan map(ResultSet rs) throws SQLException {
        Penjualan p = new Penjualan();
        p.setIdPenjualan(rs.getInt("id_penjualan"));
        p.setNoNota(rs.getString("no_nota"));
        Timestamp ts = rs.getTimestamp("tanggal");
        p.setTanggal(ts == null ? null : ts.toLocalDateTime());
        p.setIdUser(rs.getInt("id_user"));
        p.setUsername(rs.getString("username"));
        int idMember = rs.getInt("id_member");
        p.setIdMember(rs.wasNull() ? null : idMember);
        p.setKodeMember(rs.getString("kode_member"));
        p.setTotal(rs.getDouble("total"));
        p.setBayar(rs.getDouble("bayar"));
        p.setKembalian(rs.getDouble("kembalian"));
        p.setMetodeBayar(rs.getString("metode_bayar"));
        p.setDiskon(rs.getDouble("diskon"));
        return p;
    }

    @Override
    public boolean insert(Penjualan p) {
        String sql = "INSERT INTO penjualan (no_nota, tanggal, id_user, id_member, total, bayar, kembalian) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNoNota());
            ps.setTimestamp(2, Timestamp.valueOf(p.getTanggal()));
            ps.setInt(3, p.getIdUser());
            ps.setObject(4, p.getIdMember(), Types.INTEGER);
            ps.setDouble(5, p.getTotal());
            ps.setDouble(6, p.getBayar());
            ps.setDouble(7, p.getKembalian());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Penjualan p) {
        String sql = "UPDATE penjualan SET no_nota=?, tanggal=?, id_user=?, id_member=?, "
                + "total=?, bayar=?, kembalian=? WHERE id_penjualan=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNoNota());
            ps.setTimestamp(2, Timestamp.valueOf(p.getTanggal()));
            ps.setInt(3, p.getIdUser());
            ps.setObject(4, p.getIdMember(), Types.INTEGER);
            ps.setDouble(5, p.getTotal());
            ps.setDouble(6, p.getBayar());
            ps.setDouble(7, p.getKembalian());
            ps.setInt(8, p.getIdPenjualan());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM penjualan WHERE id_penjualan=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Penjualan> getAll() {
        String sql = BASE_SELECT + " ORDER BY p.tanggal DESC, p.id_penjualan DESC";
        List<Penjualan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public Penjualan getById(int id) {
        String sql = BASE_SELECT + " WHERE p.id_penjualan=?";
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
            throw new RuntimeException("Gagal ambil penjualan by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Penjualan> search(String keyword) {
        String sql = BASE_SELECT + " WHERE p.no_nota LIKE ? ORDER BY p.tanggal DESC, p.id_penjualan DESC";
        List<Penjualan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal cari penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public int saveWithDetail(Penjualan h, List<DetailPenjualan> d) {
        String sqlHeader = "INSERT INTO penjualan (no_nota, tanggal, id_user, id_member, total, bayar, kembalian, metode_bayar, diskon) "
                + "VALUES (?,?,?,?,?,?,?,?,?)";
        String sqlDetail = "INSERT INTO detail_penjualan (id_penjualan, id_buku, qty, harga_jual, subtotal) "
                + "VALUES (?,?,?,?,?)";
        String sqlStok = "UPDATE buku SET stok = stok - ? WHERE id_buku = ? AND stok >= ?";
        Connection c = null;
        try {
            c = Koneksi.getConnection();
            c.setAutoCommit(false);
            int idPenjualan;
            try (PreparedStatement ps = c.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, h.getNoNota());
                ps.setTimestamp(2, Timestamp.valueOf(h.getTanggal()));
                ps.setInt(3, h.getIdUser());
                ps.setObject(4, h.getIdMember(), Types.INTEGER);
                ps.setDouble(5, h.getTotal());
                ps.setDouble(6, h.getBayar());
                ps.setDouble(7, h.getKembalian());
                ps.setString(8, h.getMetodeBayar());
                ps.setDouble(9, h.getDiskon());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new RuntimeException("Gagal simpan penjualan: id header tidak tersedia");
                    }
                    idPenjualan = keys.getInt(1);
                }
            }
            try (PreparedStatement psDetail = c.prepareStatement(sqlDetail);
                 PreparedStatement psStok = c.prepareStatement(sqlStok)) {
                for (DetailPenjualan item : d) {
                    psDetail.setInt(1, idPenjualan);
                    psDetail.setInt(2, item.getIdBuku());
                    psDetail.setInt(3, item.getQty());
                    psDetail.setDouble(4, item.getHargaJual());
                    psDetail.setDouble(5, item.getSubtotal());
                    psDetail.executeUpdate();
                    psStok.setInt(1, item.getQty());
                    psStok.setInt(2, item.getIdBuku());
                    psStok.setInt(3, item.getQty());
                    if (psStok.executeUpdate() == 0) {
                        throw new RuntimeException("Stok tidak mencukupi untuk id_buku=" + item.getIdBuku());
                    }
                }
            }
            c.commit();
            return idPenjualan;
        } catch (SQLException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Gagal simpan penjualan (rollback gagal): " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Gagal simpan penjualan: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Gagal simpan penjualan (rollback gagal): " + ex.getMessage(), ex);
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
    public List<Penjualan> listTerbaru(int limit) {
        String sql = BASE_SELECT + " ORDER BY p.tanggal DESC, p.id_penjualan DESC LIMIT ?";
        List<Penjualan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil penjualan terbaru: " + e.getMessage(), e);
        }
    }

    @Override
    public Penjualan getByNoNota(String noNota) {
        String sql = BASE_SELECT + " WHERE p.no_nota=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, noNota);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return map(rs);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil penjualan by no nota: " + e.getMessage(), e);
        }
    }

    @Override
    public List<DetailPenjualan> getDetailByPenjualan(int idPenjualan) {
        String sql = "SELECT d.id_detail, d.id_penjualan, d.id_buku, b.kode_buku, b.judul, "
                + "d.qty, d.harga_jual, d.subtotal "
                + "FROM detail_penjualan d "
                + "JOIN buku b ON d.id_buku = b.id_buku "
                + "WHERE d.id_penjualan=? ORDER BY d.id_detail";
        List<DetailPenjualan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPenjualan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DetailPenjualan d = new DetailPenjualan();
                    d.setIdDetail(rs.getInt("id_detail"));
                    d.setIdPenjualan(rs.getInt("id_penjualan"));
                    d.setIdBuku(rs.getInt("id_buku"));
                    d.setKodeBuku(rs.getString("kode_buku"));
                    d.setJudul(rs.getString("judul"));
                    d.setQty(rs.getInt("qty"));
                    d.setHargaJual(rs.getDouble("harga_jual"));
                    d.setSubtotal(rs.getDouble("subtotal"));
                    list.add(d);
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil detail penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapPenjualan> lapPenjualan(LocalDate a, LocalDate b) {
        String sql = "SELECT p.no_nota, p.tanggal, u.nama_lengkap kasir, COALESCE(m.nama,'-') member, p.total, "
                + "p.metode_bayar, p.diskon "
                + "FROM penjualan p "
                + "JOIN users u ON p.id_user = u.id_user "
                + "LEFT JOIN member m ON p.id_member = m.id_member "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? ORDER BY p.tanggal";
        List<LapPenjualan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("tanggal");
                    list.add(new LapPenjualan(
                            rs.getString("no_nota"),
                            ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("kasir"),
                            rs.getString("member"),
                            rs.getDouble("total"),
                            rs.getString("metode_bayar"),
                            rs.getDouble("diskon")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan penjualan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapPenjualan> lapPenjualanByUser(LocalDate a, LocalDate b, int idUser) {
        String sql = "SELECT p.no_nota, p.tanggal, u.nama_lengkap kasir, COALESCE(m.nama,'-') member, p.total, "
                + "p.metode_bayar, p.diskon "
                + "FROM penjualan p "
                + "JOIN users u ON p.id_user = u.id_user "
                + "LEFT JOIN member m ON p.id_member = m.id_member "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? AND p.id_user = ? ORDER BY p.tanggal";
        List<LapPenjualan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            ps.setInt(3, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("tanggal");
                    list.add(new LapPenjualan(
                            rs.getString("no_nota"),
                            ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("kasir"),
                            rs.getString("member"),
                            rs.getDouble("total"),
                            rs.getString("metode_bayar"),
                            rs.getDouble("diskon")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan penjualan per user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapPendapatan> lapPendapatan(LocalDate a, LocalDate b) {
        String sql = "SELECT p.no_nota, p.tanggal, b.judul, d.qty, b.harga_beli, d.harga_jual "
                + "FROM detail_penjualan d "
                + "JOIN penjualan p ON d.id_penjualan = p.id_penjualan "
                + "JOIN buku b ON d.id_buku = b.id_buku "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? ORDER BY p.tanggal";
        List<LapPendapatan> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("tanggal");
                    int qty = rs.getInt("qty");
                    double hargaBeli = rs.getDouble("harga_beli");
                    double hargaJual = rs.getDouble("harga_jual");
                    double laba = (hargaJual - hargaBeli) * qty;
                    list.add(new LapPendapatan(
                            rs.getString("no_nota"),
                            ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("judul"),
                            qty, hargaBeli, hargaJual, laba));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan pendapatan: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapTerlaris> lapTerlaris(LocalDate a, LocalDate b) {
        String sql = "SELECT b.kode_buku, b.judul, SUM(d.qty) tq, SUM(d.subtotal) to_ "
                + "FROM detail_penjualan d "
                + "JOIN penjualan p ON d.id_penjualan = p.id_penjualan "
                + "JOIN buku b ON d.id_buku = b.id_buku "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? "
                + "GROUP BY b.id_buku, b.kode_buku, b.judul ORDER BY tq DESC";
        List<LapTerlaris> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LapTerlaris(
                            rs.getString("kode_buku"),
                            rs.getString("judul"),
                            rs.getInt("tq"),
                            rs.getDouble("to_")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan terlaris: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapGrafik> omzetPerHari(LocalDate a, LocalDate b) {
        String sql = "SELECT DATE_FORMAT(p.tanggal,'%d %b') label, SUM(p.total) nilai "
                + "FROM penjualan p WHERE DATE(p.tanggal) BETWEEN ? AND ? "
                + "GROUP BY DATE(p.tanggal) ORDER BY DATE(p.tanggal)";
        List<LapGrafik> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new LapGrafik(rs.getString("label"), rs.getDouble("nilai")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil omzet per hari: " + e.getMessage(), e);
        }
    }

    @Override
    public int itemTerjual(LocalDate a, LocalDate b) {
        String sql = "SELECT COALESCE(SUM(d.qty),0) FROM detail_penjualan d "
                + "JOIN penjualan p ON d.id_penjualan=p.id_penjualan "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hitung item terjual: " + e.getMessage(), e);
        }
    }

    @Override
    public List<model.LapLabaKotor> lapLabaKotor(LocalDate a, LocalDate b) {
        String sql = "SELECT p.no_nota, p.tanggal, b.kode_buku, b.judul, dp.qty, "
                + "b.harga_beli, dp.harga_jual, "
                + "(dp.qty * b.harga_beli) AS total_modal, "
                + "(dp.qty * dp.harga_jual) AS total_omset, "
                + "((dp.qty * dp.harga_jual) - (dp.qty * b.harga_beli)) AS laba_kotor "
                + "FROM detail_penjualan dp "
                + "JOIN penjualan p ON dp.id_penjualan = p.id_penjualan "
                + "JOIN buku b ON dp.id_buku = b.id_buku "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? "
                + "ORDER BY p.tanggal DESC, p.id_penjualan DESC";
        List<model.LapLabaKotor> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("tanggal");
                    java.time.LocalDateTime tgl = ts == null ? null : ts.toLocalDateTime();
                    int qty = rs.getInt("qty");
                    double beli = rs.getDouble("harga_beli");
                    double jual = rs.getDouble("harga_jual");
                    double modal = rs.getDouble("total_modal");
                    double omset = rs.getDouble("total_omset");
                    double laba = rs.getDouble("laba_kotor");
                    double margin = omset > 0 ? (laba / omset) * 100.0 : 0.0;
                    list.add(new model.LapLabaKotor(
                            rs.getString("no_nota"),
                            tgl,
                            rs.getString("kode_buku"),
                            rs.getString("judul"),
                            qty,
                            beli,
                            jual,
                            modal,
                            omset,
                            laba,
                            margin
                    ));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan laba kotor: " + e.getMessage(), e);
        }
    }
}
