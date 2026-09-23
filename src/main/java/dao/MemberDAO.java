package dao;

import java.util.List;
import model.Member;

public interface MemberDAO {
    boolean insert(Member m);
    boolean update(Member m);
    boolean delete(int id);
    List<Member> getAll();
    Member getById(int id);
    List<Member> search(String keyword);
}
