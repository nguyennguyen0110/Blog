package fa.training.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fa.training.blog.dto.PostDTO;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.security.JwtTokenUtil;
import fa.training.blog.security.JwtUserDetailsService;
import fa.training.blog.service.PostService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(PostController.class)
@WithMockUser
public class PostControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PostService postService;
    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private static PostDTO post;
    private static List<PostDTO> posts;
    private static UserDTO user;

    @BeforeAll
    static void setup() {
        user = new UserDTO();
        user.setUsername("user");
        user.setFirstName("De");
        user.setLastName("Mo");

        post = new PostDTO();
        post.setId("Post ID");
        post.setTitle("Title of post");
        post.setContent("Content of post");

        posts = new ArrayList<>();
        posts.add(post);
    }

    @Test
    @DisplayName("Find post by ID success")
    @Order(1)
    void findPostByIdSuccess() throws Exception {
        String id = post.getId();
        given(postService.findPostByID(id)).willReturn(post);

        mvc.perform(get("/post").param("id", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.title").value("Title of post"));
    }

    @Test
    @DisplayName("Find post by ID not found")
    @Order(2)
    void findPostByIdNotFound() throws Exception {
        String id = post.getId();
        given(postService.findPostByID(id)).willReturn(null);

        mvc.perform(get("/post").param("id", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("Post not found"));
    }

    @Test
    @DisplayName("Find post by owner")
    @Order(3)
    void findPostByOwner() throws Exception {
        String id = post.getId();
        given(postService.findPostByOwner(any(), any())).willReturn(posts);

        mvc.perform(get("/post").param("owner", user.getUsername()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value(id))
                .andExpect(jsonPath("$.data[0].title").value("Title of post"));
    }

    @Test
    @DisplayName("Find post by create date")
    @Order(4)
    void findPostByCreateDate() throws Exception {
        given(postService.findPostByCreateDate(any(), any())).willReturn(posts);

        mvc.perform(get("/post").param("createDate", "2020-02-02"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value(post.getId()))
                .andExpect(jsonPath("$.data[0].title").value("Title of post"));
    }

    @Test
    @DisplayName("Find post by create date invalid")
    @Order(5)
    void findPostByCreateDateInvalid() throws Exception {
        mvc.perform(get("/post").param("createDate", "2020-2-2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("402"))
                .andExpect(jsonPath("$.message", containsString("ISO format: YYYY-MM-DD")));
    }

    @Test
    @DisplayName("Find post by title")
    @Order(6)
    void findPostByTitle() throws Exception {
        String title = post.getTitle();
        given(postService.findPostByTitle(eq(title), any())).willReturn(posts);

        mvc.perform(get("/post").param("title", title))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value(post.getId()))
                .andExpect(jsonPath("$.data[0].title").value(title));
    }

    @Test
    @DisplayName("Find all post")
    @Order(7)
    void findAllPost() throws Exception {
        given(postService.findAllPost(any())).willReturn(posts);

        mvc.perform(get("/post"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value(post.getId()))
                .andExpect(jsonPath("$.data[0].title").value(post.getTitle()));
    }

    @Test
    @DisplayName("Create post success")
    @Order(8)
    void createPostSuccess() throws Exception {
        String username = user.getUsername();
        given(postService.createPost(any(), eq(username))).willReturn(post);

        mvc.perform(post("/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()));
    }

    @Test
    @DisplayName("Create post invalid")
    @Order(9)
    void createPostInvalid() throws Exception {
        PostDTO invalidPost = new PostDTO();
        invalidPost.setId("Invalid post ID");
        invalidPost.setTitle("Title of invalid post");
        invalidPost.setContent("");

        mvc.perform(post("/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPost)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("406"))
                .andExpect(jsonPath("$.message", containsString("Field error")));
    }

    @Test
    @DisplayName("Edit post success")
    @Order(10)
    void editPostSuccess() throws Exception {
        given(postService.editPost(any())).willReturn(post);

        mvc.perform(put("/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(post)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value(post.getId()))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()));
    }

    @Test
    @DisplayName("Edit post invalid")
    @Order(11)
    void editPostInvalid() throws Exception {
        PostDTO invalidPost = new PostDTO();
        invalidPost.setId("Invalid post ID");
        invalidPost.setTitle("Title of invalid post");
        invalidPost.setContent("");

        mvc.perform(put("/post")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPost)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("406"))
                .andExpect(jsonPath("$.message", containsString("Field error")));
    }

    @Test
    @DisplayName("Delete post")
    @Order(12)
    void deletePost() throws Exception {
        String postID = post.getId();
        given(postService.deletePost(postID)).willReturn(post);

        mvc.perform(delete("/post/{id}", postID).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value(postID))
                .andExpect(jsonPath("$.data.title").value(post.getTitle()));
    }
}
