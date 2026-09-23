package dao;

import java.util.List;
import model.Buku;

public interface BukuDAO {
    boolean insert(Buku b);
    boolean update(Buku b);
    boolean delete(int id);
    List<Buku> getAll();
    Buku getById(int id);
    List<Buku> search(String keyword);
    List<model.LapStok> lapDataBuku();
    List<model.LapStok> getStokMenipis();
}
