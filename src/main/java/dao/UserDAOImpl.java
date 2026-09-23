package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.User;

public class UserDAOImpl implements UserDAO {

    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setIdUser(rs.getInt("id_user"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setNamaLengkap(rs.getString("nama_lengkap"));
        u.setRole(rs.getString("role"));
        return u;
    }

    @Override
    public boolean insert(User u) {
        String sql = "INSERT INTO users (username, password, nama_lengkap, role) VALUES (?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNamaLengkap());
            ps.setString(4, u.getRole());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(User u) {
        String sql = "UPDATE users SET username=?, password=?, nama_lengkap=?, role=? WHERE id_user=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPassword());
            ps.setString(3, u.getNamaLengkap());
            ps.setString(4, u.getRole());
            ps.setInt(5, u.getIdUser());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update user: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id_user=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus user: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> getAll() {
        String sql = "SELECT id_user, username, password, nama_lengkap, role FROM users ORDER BY id_user";
        List<User> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua user: " + e.getMessage(), e);
        }
    }

    @Override
    public User getById(int id) {
        String sql = "SELECT id_user, username, password, nama_lengkap, role FROM users WHERE id_user=?";
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
            throw new RuntimeException("Gagal ambil user by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<User> search(String keyword) {
        String sql = "SELECT id_user, username, password, nama_lengkap, role FROM users "
                + "WHERE username LIKE ? OR nama_lengkap LIKE ? ORDER BY id_user";
        List<User> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String kw = "%" + keyword + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal cari user: " + e.getMessage(), e);
        }
    }
}
