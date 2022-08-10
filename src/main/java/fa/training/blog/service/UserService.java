package fa.training.blog.service;

import fa.training.blog.entity.User;
import org.hibernate.type.StringNVarcharType;

import java.util.List;

public interface UserService {
    public User createUser(User user);
    public User deleteUserById(String id);
    public User editUser(User user);
    public List<User> findAllUser();
    public User findUserById(String id);
}
