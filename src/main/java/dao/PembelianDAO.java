package dao;

import java.time.LocalDate;
import java.util.List;
import model.DetailPembelian;
import model.LapPembelian;
import model.Pembelian;

public interface PembelianDAO {
    boolean insert(Pembelian p);
    boolean update(Pembelian p);
    boolean delete(int id);
    List<Pembelian> getAll();
    Pembelian getById(int id);
    List<Pembelian> search(String keyword);
    int saveWithDetail(Pembelian h, List<DetailPembelian> d);
    List<LapPembelian> lapPembelian(LocalDate a, LocalDate b);
}
