package fa.training.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.security.JwtTokenUtil;
import fa.training.blog.security.JwtUserDetailsService;
import fa.training.blog.service.UserService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class)
@WithMockUser()
public class UserControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;
    private static UserDTO user1;
    private static List<UserDTO> users;

    @BeforeAll
    public static void setup() {
        user1 = new UserDTO("user1", "12345678", "user1@demo.com",
                "De 1", "Mo 1", "ROLE_USER");
        UserDTO user2 = new UserDTO("user2", "12345678", "user2@demo.com",
                "De 2", "Mo 2", "ROLE_USER");
        users = List.of(user1, user2);
    }

    @Test
    @DisplayName("Find user by username success")
    @Order(1)
    public void findUserByUsernameSuccess() throws Exception {
        given(userService.findUserByUsername(user1.getUsername())).willReturn(user1);

        mvc.perform(get("/user").param("username", user1.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.password").value("12345678"));
    }

    @Test
    @DisplayName("Find user by username not found")
    @Order(2)
    public void findUserByUsernameNotFound() throws Exception {
        given(userService.findUserByUsername("admin")).willReturn(null);

        mvc.perform(get("/user").param("username", user1.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("Username not found"))
                .andExpect(jsonPath("$.code").value("400"));
    }

    @Test
    @DisplayName("Find user by first name and last name")
    @Order(3)
    public void findUserByFirstNameAndLastName() throws Exception {
        given(userService.findUserByFirstNameAndLastName(eq("De"), eq("Mo"), any())).willReturn(users);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", "De");
        params.add("lastName", "Mo");

        mvc.perform(get("/user").params(params))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].password").value("12345678"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));
    }

    @Test
    @DisplayName("Find user by first name")
    @Order(4)
    public void findUserByFirstName() throws Exception {
        given(userService.findUserByFirstName(eq("De"), any())).willReturn(users);

        mvc.perform(get("/user").param("firstName", "De"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].password").value("12345678"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));
    }

    @Test
    @DisplayName("Find user by last name")
    @Order(5)
    public void findUserByLastName() throws Exception {
        given(userService.findUserByLastName(eq("Mo"), any())).willReturn(users);

        mvc.perform(get("/user").param("lastName", "Mo"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].password").value("12345678"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));
    }

    @Test
    @DisplayName("Find all user")
    @Order(6)
    public void findAllUser() throws Exception {
        given(userService.findAllUser(any())).willReturn(users);

        mvc.perform(get("/user"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].password").value("12345678"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));
    }

    @Test
    @DisplayName("Edit user success")
    @Order(7)
    public void editUserSuccess() throws Exception {
        given(userService.editUser(any())).willReturn(user1);

        mvc.perform(put("/user")
                        // Need csrf in every method exclude GET because it is enabled by default in Spring Security
                        // And we are using default Spring Security for testing, not load our WebSecurityConfig class
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("user1"));
    }

    @Test
    @DisplayName("Edit user invalid")
    @Order(8)
    public void editUserInvalid() throws Exception {
        UserDTO userDTO = new UserDTO("user1", "123", "user1@demo.com",
                "De 1", "Mo 1", "ROLE_USER");

        mvc.perform(put("/user")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message", containsString("Field error")))
                .andExpect(jsonPath("$.code").value("406"));
    }

    @Test
    @DisplayName("Delete user")
    @Order(9)
    public void deleteUser() throws Exception {
        String username = user1.getUsername();
        given(userService.deleteUserByUsername(username)).willReturn(user1);

        mvc.perform(delete("/user/{id}", username).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("user1"));
    }
}
