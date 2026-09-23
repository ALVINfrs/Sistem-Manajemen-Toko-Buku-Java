package dao;

import java.util.List;
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
}
