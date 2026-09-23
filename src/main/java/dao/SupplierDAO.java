package dao;

import java.util.List;
import model.Supplier;

public interface SupplierDAO {
    boolean insert(Supplier s);
    boolean update(Supplier s);
    boolean delete(int id);
    List<Supplier> getAll();
    Supplier getById(int id);
    List<Supplier> search(String keyword);
}
