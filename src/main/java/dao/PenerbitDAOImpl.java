package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.Penerbit;

public class PenerbitDAOImpl implements PenerbitDAO {

    private Penerbit map(ResultSet rs) throws SQLException {
        Penerbit p = new Penerbit();
        p.setIdPenerbit(rs.getInt("id_penerbit"));
        p.setNamaPenerbit(rs.getString("nama_penerbit"));
        p.setAlamat(rs.getString("alamat"));
        p.setNoTelp(rs.getString("no_telp"));
        return p;
    }

    @Override
    public boolean insert(Penerbit p) {
        String sql = "INSERT INTO penerbit (nama_penerbit, alamat, no_telp) VALUES (?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNamaPenerbit());
            ps.setString(2, p.getAlamat());
            ps.setString(3, p.getNoTelp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert penerbit: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Penerbit p) {
        String sql = "UPDATE penerbit SET nama_penerbit=?, alamat=?, no_telp=? WHERE id_penerbit=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getNamaPenerbit());
            ps.setString(2, p.getAlamat());
            ps.setString(3, p.getNoTelp());
            ps.setInt(4, p.getIdPenerbit());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update penerbit: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM penerbit WHERE id_penerbit=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus penerbit: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Penerbit> getAll() {
        String sql = "SELECT id_penerbit, nama_penerbit, alamat, no_telp FROM penerbit ORDER BY id_penerbit";
        List<Penerbit> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua penerbit: " + e.getMessage(), e);
        }
    }

    @Override
    public Penerbit getById(int id) {
        String sql = "SELECT id_penerbit, nama_penerbit, alamat, no_telp FROM penerbit WHERE id_penerbit=?";
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
            throw new RuntimeException("Gagal ambil penerbit by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Penerbit> search(String keyword) {
        String sql = "SELECT id_penerbit, nama_penerbit, alamat, no_telp FROM penerbit "
                + "WHERE nama_penerbit LIKE ? OR alamat LIKE ? OR no_telp LIKE ? ORDER BY id_penerbit";
        List<Penerbit> list = new ArrayList<>();
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
            throw new RuntimeException("Gagal cari penerbit: " + e.getMessage(), e);
        }
    }
}
