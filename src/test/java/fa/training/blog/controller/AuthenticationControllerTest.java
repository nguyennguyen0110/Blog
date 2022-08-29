package fa.training.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.security.JwtTokenUtil;
import fa.training.blog.security.JwtUserDetails;
import fa.training.blog.security.JwtUserDetailsService;
import fa.training.blog.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = AuthenticationController.class)
@WithMockUser
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;
    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;
    @MockBean
    private UserService userService;
    private static UserDTO user;

    @BeforeAll
    public static void setup() {
        user = new UserDTO("user", "12345678", "user@demo.com",
                "De", "Mo", "ROLE_USER");
    }

    @Test
    @DisplayName("Sign up success")
    @Order(1)
    public void createUserSuccess() throws Exception{
        given(userService.createUser(any())).willReturn(user);

        mvc.perform(post("/signup")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.username").value("user"));
    }

    @Test
    @DisplayName("Sign up invalid user")
    @Order(2)
    public void createUserInvalid() throws Exception{
        UserDTO userDTO = new UserDTO("user", "12345678", "demo.com",
                "De", "Mo", "ROLE_USER");

        mvc.perform(post("/signup")
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
    @DisplayName("Sign in success")
    @Order(3)
    public void createAuthenticationToken() throws Exception {
        String username = user.getUsername();
        List<GrantedAuthority> authorities = new ArrayList<>();
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
        authorities.add(authority);
        UserDetails userDetails = new JwtUserDetails(username, user.getPassword(), authorities);
        String token = "This is token create by JwtTokenUtil";
        given(jwtUserDetailsService.loadUserByUsername(username)).willReturn(userDetails);
        given(jwtTokenUtil.generateToken(any())).willReturn(token);

        mvc.perform(post("/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data").value(token));
    }

    @Test
    @DisplayName("Sign in user disable")
    @Order(4)
    public void createAuthenticationTokenUserDisable() throws Exception {
        given(authenticationManager.authenticate(any())).willThrow(DisabledException.class);

        mvc.perform(post("/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("User disabled"));
    }

    @Test
    @DisplayName("Sign in invalid credentials")
    @Order(5)
    public void createAuthenticationTokenInvalidCredentials() throws Exception {
        given(authenticationManager.authenticate(any())).willThrow(BadCredentialsException.class);

        mvc.perform(post("/signin")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("401"))
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }
}
