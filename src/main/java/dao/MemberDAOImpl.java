package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.LapGrafik;
import model.Member;

public class MemberDAOImpl implements MemberDAO {

    private Member map(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setIdMember(rs.getInt("id_member"));
        m.setKodeMember(rs.getString("kode_member"));
        m.setNama(rs.getString("nama"));
        m.setAlamat(rs.getString("alamat"));
        m.setNoTelp(rs.getString("no_telp"));
        return m;
    }

    @Override
    public boolean insert(Member m) {
        String sql = "INSERT INTO member (kode_member, nama, alamat, no_telp) VALUES (?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getKodeMember());
            ps.setString(2, m.getNama());
            ps.setString(3, m.getAlamat());
            ps.setString(4, m.getNoTelp());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert member: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Member m) {
        String sql = "UPDATE member SET kode_member=?, nama=?, alamat=?, no_telp=? WHERE id_member=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getKodeMember());
            ps.setString(2, m.getNama());
            ps.setString(3, m.getAlamat());
            ps.setString(4, m.getNoTelp());
            ps.setInt(5, m.getIdMember());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update member: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM member WHERE id_member=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus member: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Member> getAll() {
        String sql = "SELECT id_member, kode_member, nama, alamat, no_telp FROM member ORDER BY id_member";
        List<Member> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua member: " + e.getMessage(), e);
        }
    }

    @Override
    public Member getById(int id) {
        String sql = "SELECT id_member, kode_member, nama, alamat, no_telp FROM member WHERE id_member=?";
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
            throw new RuntimeException("Gagal ambil member by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Member> search(String keyword) {
        String sql = "SELECT id_member, kode_member, nama, alamat, no_telp FROM member "
                + "WHERE kode_member LIKE ? OR nama LIKE ? OR no_telp LIKE ? ORDER BY id_member";
        List<Member> list = new ArrayList<>();
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
            throw new RuntimeException("Gagal cari member: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapGrafik> countTransaksi() {
        String sql = "SELECT m.nama label, COUNT(p.id_penjualan) nilai FROM member m "
                + "LEFT JOIN penjualan p ON p.id_member=m.id_member "
                + "GROUP BY m.id_member, m.nama ORDER BY m.nama";
        List<LapGrafik> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LapGrafik(rs.getString("label"), rs.getDouble("nilai")));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hitung transaksi per member: " + e.getMessage(), e);
        }
    }
}
