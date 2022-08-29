package fa.training.blog.service;

import fa.training.blog.dto.UserDTO;
import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.repository.UserRepository;
import fa.training.blog.service.impl.UserServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @InjectMocks
    private UserServiceImpl userService;
    @Mock
    private UserRepository userRepository;
    @Spy
    private ModelMapper modelMapper;
    @Spy
    private BCryptPasswordEncoder passwordEncoder;

    private static UserDTO userDTO;
    private static List<UserDTO> userDTOS;
    private static List<User> users;
    private User user;
    private static Pageable pageable;

    @BeforeAll
    public static void setup() {
        userDTO = new UserDTO("tester", "test12345678", "tester@demo.com",
                "Test", "Ter", "ROLE_ADMIN");

        userDTOS = new ArrayList<>();
        userDTOS.add(userDTO);

        users = new ArrayList<>();

        pageable = PageRequest.of(0, 10, Sort.by("role").and(Sort.by("username")));
    }

    @BeforeEach
    public void init() {
        user = modelMapper.map(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        users.add(user);
    }

    @AfterEach
    public void teardown() {
        users.clear();
    }

    @Test
    @DisplayName("Create user successfully")
    @Order(1)
    void createUserSuccess() {
        // Given
        given(userRepository.findById(userDTO.getUsername())).willReturn(Optional.empty());
        given(userRepository.findByEmail(userDTO.getEmail())).willReturn(null);
        given(userRepository.save(any(User.class))).willReturn(user);

        // when
        UserDTO actual = userService.createUser(userDTO);
        UserDTO expected = modelMapper.map(user, UserDTO.class);

        // then
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getRole(), actual.getRole());
    }

    @Test
    @DisplayName("Create user fail throw MyException \"Username existed\"")
    @Order(2)
    void createUserThrowExceptionUsernameExisted() {
        // Given
        given(userRepository.findById(userDTO.getUsername())).willReturn(Optional.of(user));

        // when
        MyException exception = assertThrows(MyException.class, () -> userService.createUser(userDTO));

        // then
        assertEquals("Username existed", exception.getMessage());
        assertEquals("405", exception.getCode());
    }

    @Test
    @DisplayName("Create user fail throw MyException \"Email used\"")
    @Order(3)
    void createUserThrowExceptionEmailUsed() {
        // Given
        given(userRepository.findById(userDTO.getUsername())).willReturn(Optional.empty());
        given(userRepository.findByEmail(userDTO.getEmail())).willReturn(user);

        // when
        MyException exception = assertThrows(MyException.class, () -> userService.createUser(userDTO));

        // then
        assertEquals("Email used", exception.getMessage());
        assertEquals("405", exception.getCode());
    }

    @Test
    @DisplayName("Delete user by username successfully")
    @Order(4)
    void deleteUserByUsernameSuccess() {
        // Given
        String username = userDTO.getUsername();

        given(userRepository.findById(username)).willReturn(Optional.of(user));

        // when
        UserDTO actual = userService.deleteUserByUsername(username);
        UserDTO expected = modelMapper.map(user, UserDTO.class);

        // then
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getRole(), actual.getRole());
    }

    @Test
    @DisplayName("Delete user by username throw MyException \"Username not found\"")
    @Order(5)
    void deleteUserByUsernameThrowExceptionUsernameNotFound() {
        // Given
        String username = userDTO.getUsername();

        given(userRepository.findById(username)).willReturn(Optional.empty());

        // when
        MyException exception = assertThrows(MyException.class, () -> userService.deleteUserByUsername(username));

        // then
        assertEquals("Username not found", exception.getMessage());
        assertEquals("400", exception.getCode());
    }

    @Test
    @DisplayName("Edit user successfully")
    @Order(6)
    void editUserSuccess() {
        // Given
        User userInDB = new User("tester", "test12345678", "tester@demo.com",
                "Test", "Terrrrrrrrrrrrrrrrrrrrrrrr", "ROLE_ADMIN");


        given(userRepository.findById(userDTO.getUsername())).willReturn(Optional.of(userInDB));
        given(userRepository.saveAndFlush(any(User.class))).willReturn(user);

        // when
        UserDTO actual = userService.editUser(userDTO);
        UserDTO expected = modelMapper.map(user, UserDTO.class);

        // then
        assertEquals(expected.getUsername(), actual.getUsername());
        assertEquals(expected.getPassword(), actual.getPassword());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getRole(), actual.getRole());
    }

    @Test
    @DisplayName("Delete user by username throw MyException \"Username not found\"")
    @Order(7)
    void editUserThrowExceptionUsernameNotFound() {
        // Given
        given(userRepository.findById(userDTO.getUsername())).willReturn(Optional.empty());

        // when
        MyException exception = assertThrows(MyException.class, () -> userService.editUser(userDTO));

        // then
        assertEquals("Username not found", exception.getMessage());
        assertEquals("400", exception.getCode());
    }

    @Test
    @DisplayName("Delete user by username throw MyException \"Email used\"")
    @Order(8)
    void editUserThrowExceptionEmailUsed() {
        // Given
        User userInDB = new User("tester", "test12345678", "testerrrrrrrrrrrrrrrrr@demo.com",
                "Test", "Ter", "ROLE_ADMIN");

        given(userRepository.findById(userDTO.getUsername())).willReturn(Optional.of(userInDB));
        given(userRepository.findByEmail(userDTO.getEmail())).willReturn(user);

        // when
        MyException exception = assertThrows(MyException.class, () -> userService.editUser(userDTO));

        // then
        assertEquals("Email used", exception.getMessage());
        assertEquals("405", exception.getCode());
    }

    @Test
    @DisplayName("Find all users")
    @Order(9)
    void findAllUser() {
        // Given
        Page<User> page = new PageImpl<>(users);
        given(userRepository.findAll(pageable)).willReturn(page);

        // when
        List<UserDTO> actual = userService.findAllUser(pageable);

        // then
        assertEquals(userDTOS.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(userDTOS.get(0).getClass(), actual.get(0).getClass());
        assertEquals(userDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find all users empty")
    @Order(10)
    void findAllUserEmpty() {
        // Given
        Page<User> page = new PageImpl<>(new ArrayList<>());

        given(userRepository.findAll(pageable)).willReturn(page);

        // when
        List<UserDTO> actual = userService.findAllUser(pageable);

        // then
        assertEquals(0, actual.size());
    }

    @Test
    @DisplayName("Find user by first name")
    @Order(11)
    void findUserByFirstName() {
        // Given
        String firstName = userDTO.getFirstName();

        given(userRepository.findByFirstNameIgnoreCaseContains(firstName, pageable)).willReturn(users);

        // when
        List<UserDTO> actual = userService.findUserByFirstName(firstName, pageable);
        UserDTO expectedUser = modelMapper.map(user, UserDTO.class);
        List<UserDTO> expected = List.of(expectedUser);

        // then
        assertEquals(expected.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(expected.get(0).getPassword(), actual.get(0).getPassword());
        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Find user by last name")
    @Order(12)
    void findUserByLastName() {
        // Given
        String lastName = userDTO.getLastName();

        given(userRepository.findByLastNameIgnoreCaseContains(lastName, pageable)).willReturn(users);

        // when
        List<UserDTO> actual = userService.findUserByLastName(lastName, pageable);
        UserDTO expectedUser = modelMapper.map(user, UserDTO.class);
        List<UserDTO> expected = List.of(expectedUser);

        // then
        assertEquals(expected.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(expected.get(0).getPassword(), actual.get(0).getPassword());
        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Find user by first name and last name")
    @Order(13)
    void findUserByFirstNameAndLastName() {
        // Given
        String firstName = userDTO.getFirstName();
        String lastName =  userDTO.getLastName();

        given(userRepository.findByFirstNameIgnoreCaseContainsAndLastNameIgnoreCaseContains(firstName, lastName, pageable))
                .willReturn(users);

        // when
        List<UserDTO> actual = userService.findUserByFirstNameAndLastName(firstName, lastName, pageable);
        UserDTO expectedUser = modelMapper.map(user, UserDTO.class);
        List<UserDTO> expected = List.of(expectedUser);

        // then
        assertEquals(expected.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(expected.get(0).getPassword(), actual.get(0).getPassword());
        assertEquals(expected.size(), actual.size());
    }
}
