package dao;

import java.time.LocalDate;
import java.util.List;
import model.LapRetur;
import model.Retur;

public interface ReturDAO {
    boolean insert(Retur r);
    boolean update(Retur r);
    boolean delete(int id);
    List<Retur> getAll();
    Retur getById(int id);
    List<Retur> search(String keyword);
    boolean saveRetur(Retur r);
    int getReturQty(int idPenjualan, int idBuku);
    List<LapRetur> lapRetur(LocalDate dari, LocalDate sampai);
}
