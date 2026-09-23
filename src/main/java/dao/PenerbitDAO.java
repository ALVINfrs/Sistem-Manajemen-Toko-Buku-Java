package dao;

import java.util.List;
import model.Penerbit;

public interface PenerbitDAO {
    boolean insert(Penerbit p);
    boolean update(Penerbit p);
    boolean delete(int id);
    List<Penerbit> getAll();
    Penerbit getById(int id);
    List<Penerbit> search(String keyword);
}
