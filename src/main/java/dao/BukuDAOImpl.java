package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import koneksi.Koneksi;
import model.Buku;
import model.LapGrafik;
import model.LapStok;

public class BukuDAOImpl implements BukuDAO {

    private static final String BASE_SELECT =
            "SELECT b.id_buku, b.kode_buku, b.judul, b.penulis, b.id_penerbit, b.id_kategori, "
            + "p.nama_penerbit, k.nama_kategori, b.harga_beli, b.harga_jual, b.stok "
            + "FROM buku b "
            + "LEFT JOIN penerbit p ON b.id_penerbit = p.id_penerbit "
            + "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori";

    private Buku map(ResultSet rs) throws SQLException {
        Buku b = new Buku();
        b.setIdBuku(rs.getInt("id_buku"));
        b.setKodeBuku(rs.getString("kode_buku"));
        b.setJudul(rs.getString("judul"));
        b.setPenulis(rs.getString("penulis"));
        int idPenerbit = rs.getInt("id_penerbit");
        b.setIdPenerbit(rs.wasNull() ? null : idPenerbit);
        int idKategori = rs.getInt("id_kategori");
        b.setIdKategori(rs.wasNull() ? null : idKategori);
        b.setNamaPenerbit(rs.getString("nama_penerbit"));
        b.setNamaKategori(rs.getString("nama_kategori"));
        b.setHargaBeli(rs.getDouble("harga_beli"));
        b.setHargaJual(rs.getDouble("harga_jual"));
        b.setStok(rs.getInt("stok"));
        return b;
    }

    private LapStok mapLapStok(ResultSet rs) throws SQLException {
        LapStok l = new LapStok();
        l.setKodeBuku(rs.getString("kode_buku"));
        l.setJudul(rs.getString("judul"));
        l.setKategori(rs.getString("kategori"));
        l.setPenerbit(rs.getString("penerbit"));
        l.setHargaBeli(rs.getDouble("harga_beli"));
        l.setHargaJual(rs.getDouble("harga_jual"));
        l.setStok(rs.getInt("stok"));
        return l;
    }

    @Override
    public boolean insert(Buku b) {
        String sql = "INSERT INTO buku (kode_buku, judul, penulis, id_penerbit, id_kategori, "
                + "harga_beli, harga_jual, stok) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getKodeBuku());
            ps.setString(2, b.getJudul());
            ps.setString(3, b.getPenulis());
            ps.setObject(4, b.getIdPenerbit(), Types.INTEGER);
            ps.setObject(5, b.getIdKategori(), Types.INTEGER);
            ps.setDouble(6, b.getHargaBeli());
            ps.setDouble(7, b.getHargaJual());
            ps.setInt(8, b.getStok());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal insert buku: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Buku b) {
        String sql = "UPDATE buku SET kode_buku=?, judul=?, penulis=?, id_penerbit=?, id_kategori=?, "
                + "harga_beli=?, harga_jual=?, stok=? WHERE id_buku=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getKodeBuku());
            ps.setString(2, b.getJudul());
            ps.setString(3, b.getPenulis());
            ps.setObject(4, b.getIdPenerbit(), Types.INTEGER);
            ps.setObject(5, b.getIdKategori(), Types.INTEGER);
            ps.setDouble(6, b.getHargaBeli());
            ps.setDouble(7, b.getHargaJual());
            ps.setInt(8, b.getStok());
            ps.setInt(9, b.getIdBuku());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal update buku: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM buku WHERE id_buku=?";
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hapus buku: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Buku> getAll() {
        String sql = BASE_SELECT + " ORDER BY b.id_buku";
        List<Buku> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil semua buku: " + e.getMessage(), e);
        }
    }

    @Override
    public Buku getById(int id) {
        String sql = BASE_SELECT + " WHERE b.id_buku=?";
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
            throw new RuntimeException("Gagal ambil buku by id: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Buku> search(String keyword) {
        String sql = BASE_SELECT + " WHERE b.kode_buku LIKE ? OR b.judul LIKE ? OR b.penulis LIKE ? "
                + "ORDER BY b.id_buku";
        List<Buku> list = new ArrayList<>();
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
            throw new RuntimeException("Gagal cari buku: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapStok> lapDataBuku() {
        String sql = "SELECT b.kode_buku, b.judul, COALESCE(k.nama_kategori,'-') kategori, "
                + "COALESCE(p.nama_penerbit,'-') penerbit, b.harga_beli, b.harga_jual, b.stok "
                + "FROM buku b "
                + "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori "
                + "LEFT JOIN penerbit p ON b.id_penerbit = p.id_penerbit "
                + "ORDER BY b.judul";
        List<LapStok> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLapStok(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil laporan data buku: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapStok> getStokMenipis() {
        String sql = "SELECT b.kode_buku, b.judul, COALESCE(k.nama_kategori,'-') kategori, "
                + "COALESCE(p.nama_penerbit,'-') penerbit, b.harga_beli, b.harga_jual, b.stok "
                + "FROM buku b "
                + "LEFT JOIN kategori k ON b.id_kategori = k.id_kategori "
                + "LEFT JOIN penerbit p ON b.id_penerbit = p.id_penerbit "
                + "WHERE b.stok <= 5 ORDER BY b.stok, b.judul";
        List<LapStok> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapLapStok(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal ambil stok menipis: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LapGrafik> countByKategori() {
        String sql = "SELECT k.nama_kategori label, COUNT(b.id_buku) nilai FROM kategori k "
                + "LEFT JOIN buku b ON b.id_kategori=k.id_kategori "
                + "GROUP BY k.id_kategori, k.nama_kategori ORDER BY k.nama_kategori";
        List<LapGrafik> list = new ArrayList<>();
        try (Connection c = Koneksi.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new LapGrafik(rs.getString("label"), rs.getDouble("nilai")));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Gagal hitung buku per kategori: " + e.getMessage(), e);
        }
    }
}
