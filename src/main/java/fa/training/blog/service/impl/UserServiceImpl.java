package fa.training.blog.service.impl;

import fa.training.blog.dto.UserDTO;
import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.repository.UserRepository;
import fa.training.blog.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        UserDTO userInDatabase = findUserByUsername(userDTO.getUsername());
        if(userInDatabase == null){
            userInDatabase = findUserByEmail(userDTO.getEmail());
            if (userInDatabase != null){
                throw new MyException("400", "Email used");
            }
            String encodedPassword = passwordEncoder().encode(userDTO.getPassword());
            userDTO.setPassword(encodedPassword);
            User savedUser = userRepository.save(modelMapper.map(userDTO, User.class));
            return modelMapper.map(savedUser, UserDTO.class);
        } else {
            throw new MyException("400", "Username existed");
        }
    }

    @Override
    public UserDTO deleteUserByUsername(String username) {
        UserDTO userToDelete = findUserByUsername(username);
        if(userToDelete != null){
            userRepository.deleteById(username);
            return userToDelete;
        } else {
            throw new MyException("400", "Username not found");
        }
    }

    @Override
    public UserDTO editUser(UserDTO userDTO) {
        UserDTO userToEdit = findUserByUsername(userDTO.getUsername());
        if(userToEdit != null){
            // If change email then need to check if new email used
            if (!userDTO.getEmail().equals(userToEdit.getEmail())) {
                UserDTO emailUsed = findUserByEmail(userDTO.getEmail());
                if (emailUsed != null){
                    throw new MyException("400", "Email used");
                }
            }
            String encodedPassword = passwordEncoder().encode(userDTO.getPassword());
            userDTO.setPassword(encodedPassword);
            User editedUser = userRepository.saveAndFlush(modelMapper.map(userDTO, User.class));
            return modelMapper.map(editedUser, UserDTO.class);
        } else {
            throw new MyException("400", "Username not found");
        }
    }

    @Override
    public List<UserDTO> findAllUser(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.stream().map(user -> modelMapper.map(user, UserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public UserDTO findUserByUsername(String username) {
        Optional<User> user = userRepository.findById(username);
        return user.map(value -> modelMapper.map(value, UserDTO.class)).orElse(null);
    }

    @Override
    public UserDTO findUserByEmail(String email) {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return null;
        }
        return modelMapper.map(user, UserDTO.class);
    }

    @Override
    public List<UserDTO> findUserByFirstName(String firstName, Pageable pageable) {
        List<User> users = userRepository.findByFirstNameIgnoreCaseContains(firstName, pageable);
        return users.stream().map(user -> modelMapper.map(user, UserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findUserByLastName(String lastName, Pageable pageable) {
        List<User> users = userRepository.findByLastNameIgnoreCaseContains(lastName, pageable);
        return users.stream().map(user -> modelMapper.map(user, UserDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<UserDTO> findUserByFirstNameAndLastName(String firstName, String lastName, Pageable pageable) {
        List<User> users = userRepository.findByFirstNameIgnoreCaseContainsAndLastNameIgnoreCaseContains(firstName, lastName, pageable);
        return users.stream().map(user -> modelMapper.map(user, UserDTO.class)).collect(Collectors.toList());
    }
}
