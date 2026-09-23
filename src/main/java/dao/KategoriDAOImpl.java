package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.Kategori;

public class KategoriDAOImpl implements KategoriDAO {

    private Kategori map(ResultSet rs) throws SQLException {
        Kategori k = new Kategori();
        k.setIdKategori(rs.getInt("id_kategori"));
        k.setNamaKategori(rs.getString("nama_kategori"));
        k.setDeskripsi(rs.getString("deskripsi"));
        return k;
    }

    @Override
    public boolean insert(Kategori k) {
        String sql = "INSERT INTO kategori (nama_kategori, deskripsi) VALUES (?, ?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKategori());
            ps.setString(2, k.getDeskripsi());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert kategori: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Kategori k) {
        String sql = "UPDATE kategori SET nama_kategori=?, deskripsi=? WHERE id_kategori=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getNamaKategori());
            ps.setString(2, k.getDeskripsi());
            ps.setInt(3, k.getIdKategori());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update kategori: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM kategori WHERE id_kategori=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus kategori: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Kategori> getAll() {
        String sql = "SELECT id_kategori, nama_kategori, deskripsi FROM kategori ORDER BY id_kategori";
        List<Kategori> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua kategori: " + e.getMessage(), e);
        }
    }

    @Override
    public Kategori getById(int id) {
        String sql = "SELECT id_kategori, nama_kategori, deskripsi FROM kategori WHERE id_kategori=?";
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
            throw new RuntimeException("Gagal ambil kategori by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Kategori> search(String keyword) {
        String sql = "SELECT id_kategori, nama_kategori, deskripsi FROM kategori "
                + "WHERE nama_kategori LIKE ? ORDER BY id_kategori";
        List<Kategori> list = new ArrayList<>();
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
            throw new RuntimeException("Gagal cari kategori: " + e.getMessage(), e);
        }
    }
}
