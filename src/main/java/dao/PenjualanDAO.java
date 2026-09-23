package dao;

import java.time.LocalDate;
import java.util.List;
import model.DetailPenjualan;
import model.LapPendapatan;
import model.LapPenjualan;
import model.LapTerlaris;
import model.Penjualan;

public interface PenjualanDAO {
    boolean insert(Penjualan p);
    boolean update(Penjualan p);
    boolean delete(int id);
    List<Penjualan> getAll();
    Penjualan getById(int id);
    List<Penjualan> search(String keyword);
    int saveWithDetail(Penjualan h, List<DetailPenjualan> d);
    Penjualan getByNoNota(String noNota);
    List<DetailPenjualan> getDetailByPenjualan(int idPenjualan);
    List<LapPenjualan> lapPenjualan(LocalDate a, LocalDate b);
    List<LapPendapatan> lapPendapatan(LocalDate a, LocalDate b);
    List<LapTerlaris> lapTerlaris(LocalDate a, LocalDate b);
}
