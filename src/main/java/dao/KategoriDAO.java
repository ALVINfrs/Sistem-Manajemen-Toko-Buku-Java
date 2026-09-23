package dao;

import java.util.List;
import model.Kategori;

public interface KategoriDAO {
    boolean insert(Kategori k);
    boolean update(Kategori k);
    boolean delete(int id);
    List<Kategori> getAll();
    Kategori getById(int id);
    List<Kategori> search(String keyword);
}
