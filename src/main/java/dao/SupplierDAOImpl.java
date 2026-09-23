package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.Supplier;

public class SupplierDAOImpl implements SupplierDAO {

    private Supplier map(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setIdSupplier(rs.getInt("id_supplier"));
        s.setNamaSupplier(rs.getString("nama_supplier"));
        s.setAlamat(rs.getString("alamat"));
        s.setNoTelp(rs.getString("no_telp"));
        return s;
    }

    @Override
    public boolean insert(Supplier s) {
        String sql = "INSERT INTO supplier (nama_supplier, alamat, no_telp) VALUES (?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getNamaSupplier());
            ps.setString(2, s.getAlamat());
            ps.setString(3, s.getNoTelp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Supplier s) {
        String sql = "UPDATE supplier SET nama_supplier=?, alamat=?, no_telp=? WHERE id_supplier=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getNamaSupplier());
            ps.setString(2, s.getAlamat());
            ps.setString(3, s.getNoTelp());
            ps.setInt(4, s.getIdSupplier());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM supplier WHERE id_supplier=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Supplier> getAll() {
        String sql = "SELECT id_supplier, nama_supplier, alamat, no_telp FROM supplier ORDER BY id_supplier";
        List<Supplier> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua supplier: " + e.getMessage(), e);
        }
    }

    @Override
    public Supplier getById(int id) {
        String sql = "SELECT id_supplier, nama_supplier, alamat, no_telp FROM supplier WHERE id_supplier=?";
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
            throw new RuntimeException("Gagal ambil supplier by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Supplier> search(String keyword) {
        String sql = "SELECT id_supplier, nama_supplier, alamat, no_telp FROM supplier "
                + "WHERE nama_supplier LIKE ? OR alamat LIKE ? OR no_telp LIKE ? ORDER BY id_supplier";
        List<Supplier> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal cari supplier: " + e.getMessage(), e);
        }
    }
}
