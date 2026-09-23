package dao;

import java.util.List;
import model.User;

public interface UserDAO {
    boolean insert(User u);
    boolean update(User u);
    boolean delete(int id);
    List<User> getAll();
    User getById(int id);
    List<User> search(String keyword);
}
