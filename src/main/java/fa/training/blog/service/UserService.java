package fa.training.blog.service;

import fa.training.blog.dto.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    public UserDTO createUser(UserDTO user);
    public UserDTO deleteUserByUsername(String username);
    public UserDTO editUser(UserDTO user);
    public List<UserDTO> findAllUser(Pageable pageable);
    public UserDTO findUserByUsername(String username);
    public UserDTO findUserByEmail(String email);
    public List<UserDTO> findUserByFirstName(String firstName, Pageable pageable);
    public List<UserDTO> findUserByLastName(String lastName, Pageable pageable);
    public List<UserDTO> findUserByFirstNameAndLastName(String firstName, String lastName, Pageable pageable);
}
