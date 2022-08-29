package fa.training.blog.controller;

import fa.training.blog.security.JwtTokenUtil;
import fa.training.blog.security.JwtUserDetailsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HelloController.class)
@WithMockUser
public class HelloControllerTest {
    @Autowired
    private MockMvc mvc;
    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    @DisplayName("Hello")
    void hello() throws Exception {
        mvc.perform(get("/"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data").value("Hello Spring Boot Restful API"));
    }
}
