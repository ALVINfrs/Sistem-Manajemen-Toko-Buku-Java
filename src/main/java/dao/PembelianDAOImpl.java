package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.DetailPembelian;
import model.LapPembelian;
import model.Pembelian;

public class PembelianDAOImpl implements PembelianDAO {

    private static final String BASE_SELECT =
            "SELECT p.id_pembelian, p.no_faktur, p.tanggal, p.id_supplier, s.nama_supplier, "
            + "p.id_user, u.username, p.total "
            + "FROM pembelian p "
            + "JOIN supplier s ON p.id_supplier = s.id_supplier "
            + "JOIN users u ON p.id_user = u.id_user";

    private Pembelian map(ResultSet rs) throws SQLException {
        Pembelian p = new Pembelian();
        p.setIdPembelian(rs.getInt("id_pembelian"));
        p.setNoFaktur(rs.getString("no_faktur"));
        Timestamp ts = rs.getTimestamp("tanggal");
        p.setTanggal(ts == null ? null : ts.toLocalDateTime());
        p.setIdSupplier(rs.getInt("id_supplier"));
        p.setNamaSupplier(rs.getString("nama_supplier"));
        p.setIdUser(rs.getInt("id_user"));
        p.setUsername(rs.getString("username"));
        p.setTotal(rs.getDouble("total"));
        return p;
    }

    @Override
    public boolean insert(Pembelian p) {
        String sql = "INSERT INTO pembelian (no_faktur, tanggal, id_supplier, id_user, total) "
                + "VALUES (?,?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNoFaktur());
            ps.setTimestamp(2, Timestamp.valueOf(p.getTanggal()));
            ps.setInt(3, p.getIdSupplier());
            ps.setInt(4, p.getIdUser());
            ps.setDouble(5, p.getTotal());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert pembelian: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Pembelian p) {
        String sql = "UPDATE pembelian SET no_faktur=?, tanggal=?, id_supplier=?, id_user=?, total=? "
                + "WHERE id_pembelian=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNoFaktur());
            ps.setTimestamp(2, Timestamp.valueOf(p.getTanggal()));
            ps.setInt(3, p.getIdSupplier());
            ps.setInt(4, p.getIdUser());
            ps.setDouble(5, p.getTotal());
            ps.setInt(6, p.getIdPembelian());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update pembelian: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM pembelian WHERE id_pembelian=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus pembelian: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pembelian> getAll() {
        String sql = BASE_SELECT + " ORDER BY p.tanggal DESC, p.id_pembelian DESC";
        List<Pembelian> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua pembelian: " + e.getMessage(), e);
        }
    }

    @Override
    public Pembelian getById(int id) {
        String sql = BASE_SELECT + " WHERE p.id_pembelian=?";
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
            throw new RuntimeException("Gagal ambil pembelian by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Pembelian> search(String keyword) {
        String sql = BASE_SELECT + " WHERE p.no_faktur LIKE ? ORDER BY p.tanggal DESC, p.id_pembelian DESC";
        List<Pembelian> list = new ArrayList<>();
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
            throw new RuntimeException("Gagal cari pembelian: " + e.getMessage(), e);
        }
    }

    @Override
    public int saveWithDetail(Pembelian h, List<DetailPembelian> d) {
        String sqlHeader = "INSERT INTO pembelian (no_faktur, tanggal, id_supplier, id_user, total) "
                + "VALUES (?,?,?,?,?)";
        String sqlDetail = "INSERT INTO detail_pembelian (id_pembelian, id_buku, qty, harga_beli, subtotal) "
                + "VALUES (?,?,?,?,?)";
        String sqlStok = "UPDATE buku SET stok = stok + ? WHERE id_buku = ?";
        Connection c = null;
        try {
            c = Koneksi.getConnection();
            c.setAutoCommit(false);
            int idPembelian;
            try (PreparedStatement ps = c.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, h.getNoFaktur());
                ps.setTimestamp(2, Timestamp.valueOf(h.getTanggal()));
                ps.setInt(3, h.getIdSupplier());
                ps.setInt(4, h.getIdUser());
                ps.setDouble(5, h.getTotal());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new RuntimeException("Gagal simpan pembelian: id header tidak tersedia");
                    }
                    idPembelian = keys.getInt(1);
                }
            }
            try (PreparedStatement psDetail = c.prepareStatement(sqlDetail);
                 PreparedStatement psStok = c.prepareStatement(sqlStok)) {
                for (DetailPembelian item : d) {
                    psDetail.setInt(1, idPembelian);
                    psDetail.setInt(2, item.getIdBuku());
                    psDetail.setInt(3, item.getQty());
                    psDetail.setDouble(4, item.getHargaBeli());
                    psDetail.setDouble(5, item.getSubtotal());
                    psDetail.executeUpdate();
                    psStok.setInt(1, item.getQty());
                    psStok.setInt(2, item.getIdBuku());
                    psStok.executeUpdate();
                }
            }
            c.commit();
            return idPembelian;
        } catch (SQLException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Gagal simpan pembelian (rollback gagal): " + ex.getMessage(), ex);
                }
            }
            throw new RuntimeException("Gagal simpan pembelian: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (c != null) {
                try {
                    c.rollback();
                } catch (SQLException ex) {
                    throw new RuntimeException("Gagal simpan pembelian (rollback gagal): " + ex.getMessage(), ex);
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
    public List<LapPembelian> lapPembelian(LocalDate a, LocalDate b) {
        String sql = "SELECT p.no_faktur, p.tanggal, s.nama_supplier supplier, u.nama_lengkap kasir, p.total "
                + "FROM pembelian p "
                + "JOIN supplier s ON p.id_supplier = s.id_supplier "
                + "JOIN users u ON p.id_user = u.id_user "
                + "WHERE DATE(p.tanggal) BETWEEN ? AND ? ORDER BY p.tanggal";
        List<LapPembelian> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(a));
            ps.setDate(2, java.sql.Date.valueOf(b));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("tanggal");
                    list.add(new LapPembelian(
                            rs.getString("no_faktur"),
                            ts == null ? null : ts.toLocalDateTime(),
                            rs.getString("supplier"),
                            rs.getString("kasir"),
                            rs.getDouble("total")));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan pembelian: " + e.getMessage(), e);
        }
    }
}
