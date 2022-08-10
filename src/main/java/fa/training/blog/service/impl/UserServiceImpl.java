package fa.training.blog.service.impl;

import fa.training.blog.entity.User;
import fa.training.blog.repository.UserRepository;
import fa.training.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public User createUser(User user) {
        User userInDatabase = findUserById(user.getUsername());
        if(userInDatabase == null){
            return userRepository.save(user);
        } else {
            return null;
        }
    }

    @Override
    public User deleteUserById(String username) {
        User userToDelete = findUserById(username);
        if(userToDelete != null){
            userRepository.deleteById(username);
            return userToDelete;
        } else {
            return null;
        }
    }

    @Override
    public User editUser(User user) {
        User userToEdit = findUserById(user.getUsername());
        if(userToEdit != null){
            return userRepository.saveAndFlush(user);
        } else {
            return null;
        }
    }

    @Override
    public List<User> findAllUser() {
        return userRepository.findAll();
    }

    @Override
    public User findUserById(String username) {
        Optional<User> user = userRepository.findById(username);
        return user.orElse(null);
    }
}
