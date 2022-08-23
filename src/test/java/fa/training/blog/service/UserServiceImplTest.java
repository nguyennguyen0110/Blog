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
import static org.mockito.Mockito.when;

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
    private static List<UserDTO> userDTOS = new ArrayList<>();
    private User user;
    private List<User> users = new ArrayList<>();
    private static Pageable pageable;

    @BeforeAll
    public static void setup() {
        userDTO = new UserDTO("tester", "test12345678", "tester@demo.com",
                "Test", "Ter", "ROLE_ADMIN");
        userDTOS.add(userDTO);

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

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // then
        UserDTO actual = userService.createUser(userDTO);
        UserDTO expected = modelMapper.map(user, UserDTO.class);

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

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.of(user));
        MyException exception = assertThrows(MyException.class, () -> userService.createUser(userDTO));

        // then
        assertEquals("Username existed", exception.getErrMsg());
    }

    @Test
    @DisplayName("Create user fail throw MyException \"Email used\"")
    @Order(3)
    void createUserThrowExceptionEmailUsed() {
        // Given

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        MyException exception = assertThrows(MyException.class, () -> userService.createUser(userDTO));

        // then
        assertEquals("Email used", exception.getErrMsg());
    }

    @Test
    @DisplayName("Delete user by username successfully")
    @Order(4)
    void deleteUserByUsernameSuccess() {
        // Given

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.of(user));

        // then
        UserDTO actual = userService.deleteUserByUsername(userDTO.getUsername());
        UserDTO expected = modelMapper.map(user, UserDTO.class);

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

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.empty());
        MyException exception = assertThrows(MyException.class, () -> userService.deleteUserByUsername(userDTO.getUsername()));

        // then
        assertEquals("Username not found", exception.getErrMsg());
    }

    @Test
    @DisplayName("Edit user successfully")
    @Order(6)
    void editUserSuccess() {
        // Given
        User userInDB = new User("tester", "test12345678", "tester@demo.com",
                "Test", "Terrrrrrrrrrrrrrrrrrrrrrrr", "ROLE_ADMIN");

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.of(userInDB));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(user);

        // then
        UserDTO actual = userService.editUser(userDTO);
        UserDTO expected = modelMapper.map(user, UserDTO.class);

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

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.empty());
        MyException exception = assertThrows(MyException.class, () -> userService.editUser(userDTO));

        // then
        assertEquals("Username not found", exception.getErrMsg());
    }

    @Test
    @DisplayName("Delete user by username throw MyException \"Email used\"")
    @Order(8)
    void editUserThrowExceptionEmailUsed() {
        // Given
        User userInDB = new User("tester", "test12345678", "testerrrrrrrrrrrrrrrrr@demo.com",
                "Test", "Ter", "ROLE_ADMIN");

        // when
        when(userRepository.findById(userDTO.getUsername())).thenReturn(Optional.of(userInDB));
        when(userRepository.findByEmail(userDTO.getEmail())).thenReturn(user);
        MyException exception = assertThrows(MyException.class, () -> userService.editUser(userDTO));

        // then
        assertEquals("Email used", exception.getErrMsg());
    }

    @Test
    @DisplayName("Find all users")
    @Order(9)
    void findAllUser() {
        // Given
        Page<User> page = new PageImpl<>(users);

        // when
        when(userRepository.findAll(pageable)).thenReturn(page);

        // then
        List<UserDTO> actual = userService.findAllUser(pageable);

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

        // when
        when(userRepository.findAll(pageable)).thenReturn(page);

        // then
        List<UserDTO> actual = userService.findAllUser(pageable);

        assertEquals(0, actual.size());
    }

    @Test
    @DisplayName("Find user by first name")
    @Order(11)
    void findUserByFirstName() {
        // Given

        // when
        when(userRepository.findByFirstNameIgnoreCaseContains(userDTO.getFirstName(), pageable)).thenReturn(users);

        // then
        List<UserDTO> actual = userService.findUserByFirstName(userDTO.getFirstName(), pageable);
        UserDTO expectedUser = modelMapper.map(user, UserDTO.class);
        List<UserDTO> expected = List.of(expectedUser);

        assertEquals(expected.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(expected.get(0).getPassword(), actual.get(0).getPassword());
        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Find user by last name")
    @Order(12)
    void findUserByLastName() {
        // Given

        // when
        when(userRepository.findByLastNameIgnoreCaseContains(userDTO.getLastName(), pageable)).thenReturn(users);

        // then
        List<UserDTO> actual = userService.findUserByLastName(userDTO.getLastName(), pageable);
        UserDTO expectedUser = modelMapper.map(user, UserDTO.class);
        List<UserDTO> expected = List.of(expectedUser);

        assertEquals(expected.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(expected.get(0).getPassword(), actual.get(0).getPassword());
        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Find user by first name and last name")
    @Order(13)
    void findUserByFirstNameAndLastName() {
        // Given

        // when
        when(userRepository.findByFirstNameIgnoreCaseContainsAndLastNameIgnoreCaseContains(userDTO.getFirstName(), userDTO.getLastName(), pageable)).thenReturn(users);

        // then
        List<UserDTO> actual = userService.findUserByFirstNameAndLastName(userDTO.getFirstName(), userDTO.getLastName(), pageable);
        UserDTO expectedUser = modelMapper.map(user, UserDTO.class);
        List<UserDTO> expected = List.of(expectedUser);

        assertEquals(expected.get(0).getUsername(), actual.get(0).getUsername());
        assertEquals(expected.get(0).getPassword(), actual.get(0).getPassword());
        assertEquals(expected.size(), actual.size());
    }
}
