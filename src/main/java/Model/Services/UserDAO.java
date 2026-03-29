package Model.Services;

import Model.Entities.User;
import java.util.List;

public interface UserDAO {
    void save(User user);

    User findById(int id);
    User findByEmail(String email);
    List<User> findAll();

    boolean update(User user);
    boolean delete(int id);

    User validateLogin(String email, String password);
}