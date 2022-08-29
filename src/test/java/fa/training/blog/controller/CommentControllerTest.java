package fa.training.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fa.training.blog.dto.CommentDTO;
import fa.training.blog.dto.PostDTO;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.security.JwtTokenUtil;
import fa.training.blog.security.JwtUserDetailsService;
import fa.training.blog.service.CommentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
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

@WebMvcTest(CommentController.class)
@WithMockUser
// Need remove filters so can test anonymous user get comment
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private CommentService commentService;
    @MockBean
    private JwtUserDetailsService jwtUserDetailsService;
    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    private static CommentDTO comment;
    private static List<CommentDTO> comments;
    private static UserDTO user;
    private static PostDTO post;

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

        comment = new CommentDTO();
        comment.setId("Comment ID");
        comment.setContent("Content of comment");

        comments = new ArrayList<>();
        comments.add(comment);
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Find by ID anonymous")
    @Order(1)
    void findByIdAnonymous() throws Exception {
        String id = comment.getId();
        given(commentService.findCommentByIDAndDeleted(id)).willReturn(comment);

        mvc.perform(get("/comment").param("id", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.content").value("Content of comment"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Find by ID anonymous comment not found")
    @Order(2)
    void findByIdAnonymousCommentNotFound() throws Exception {
        String id = comment.getId();
        given(commentService.findCommentByIDAndDeleted(id)).willReturn(null);

        mvc.perform(get("/comment").param("id", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("Comment not found"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Find by owner anonymous")
    @Order(3)
    void findByOwnerAnonymous() throws Exception {
        String username = user.getUsername();
        given(commentService.findCommentByOwnerAndDeleted(eq(username), any())).willReturn(comments);

        mvc.perform(get("/comment").param("owner", username))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Find by post anonymous")
    @Order(4)
    void findByPostAnonymous() throws Exception {
        String postID = post.getId();
        given(commentService.findCommentByPostAndDeleted(eq(postID), any())).willReturn(comments);

        mvc.perform(get("/comment").param("post", postID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Find by create date anonymous")
    @Order(5)
    void findByCreateDateAnonymous() throws Exception {
        given(commentService.findCommentByCreateDateAndDeleted(any(), any())).willReturn(comments);

        mvc.perform(get("/comment").param("createDate", "2020-02-02"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Find all comment anonymous")
    @Order(6)
    void findAllCommentAnonymous() throws Exception {
        given(commentService.findCommentByDeleted(any())).willReturn(comments);

        mvc.perform(get("/comment"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @DisplayName("Find by ID user")
    @Order(7)
    void findByIdUser() throws Exception {
        String id = comment.getId();
        given(commentService.findCommentByIDAndDeleted(id)).willReturn(comment);

        mvc.perform(get("/comment").param("id", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value(id))
                .andExpect(jsonPath("$.data.content").value("Content of comment"));
    }

    @Test
    @DisplayName("Find by owner user")
    @Order(8)
    void findByOwnerUser() throws Exception {
        String username = user.getUsername();
        given(commentService.findCommentByOwnerAndDeleted(eq(username), any())).willReturn(comments);

        mvc.perform(get("/comment").param("owner", username))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @DisplayName("Find by post user")
    @Order(9)
    void findByPostUser() throws Exception {
        String postID = post.getId();
        given(commentService.findCommentByPostAndDeleted(eq(postID), any())).willReturn(comments);

        mvc.perform(get("/comment").param("post", postID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @DisplayName("Find by create date user")
    @Order(10)
    void findByCreateDateUser() throws Exception {
        given(commentService.findCommentByCreateDateAndDeleted(any(), any())).willReturn(comments);

        mvc.perform(get("/comment").param("createDate", "2020-02-02"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @DisplayName("Find all comment user")
    @Order(11)
    void findAllCommentUser() throws Exception {
        given(commentService.findCommentByDeleted(any())).willReturn(comments);

        mvc.perform(get("/comment"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Find by ID admin comment not found")
    @Order(12)
    void findByIdAdminCommentNotFound() throws Exception {
        String id = comment.getId();
        given(commentService.findCommentByID(id)).willReturn(null);

        mvc.perform(get("/comment").param("id", id))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("Comment not found"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Find by owner admin")
    @Order(13)
    void findByOwnerAdmin() throws Exception {
        String username = user.getUsername();
        given(commentService.findCommentByOwner(eq(username), any())).willReturn(comments);

        mvc.perform(get("/comment").param("owner", username))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Find by post admin")
    @Order(14)
    void findByPostAdmin() throws Exception {
        String postID = post.getId();
        given(commentService.findCommentByPost(eq(postID), any())).willReturn(comments);

        mvc.perform(get("/comment").param("post", postID))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Find by create date admin")
    @Order(15)
    void findByCreateDateAdmin() throws Exception {
        given(commentService.findCommentByCreateDate(any(), any())).willReturn(comments);

        mvc.perform(get("/comment").param("createDate", "2020-02-02"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Find by create date invalid admin")
    @Order(16)
    void findByCreateDateInvalidAdmin() throws Exception {
        mvc.perform(get("/comment").param("createDate", "2020-2-2"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("402"))
                .andExpect(jsonPath("$.message", containsString("ISO format: YYYY-MM-DD")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("Find all comment admin")
    @Order(17)
    void findAllCommentAdmin() throws Exception {
        given(commentService.findAllComment(any())).willReturn(comments);

        mvc.perform(get("/comment"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data[0].id").value("Comment ID"))
                .andExpect(jsonPath("$.data[0].content").value("Content of comment"));
    }

    @Test
    @DisplayName("Create comment success")
    @Order(18)
    void createCommentSuccess() throws Exception {
        String postID = post.getId();
        String username = user.getUsername();
        given(commentService.createComment(any(), eq(postID), eq(username))).willReturn(comment);

        mvc.perform(post("/comment")
                        .with(csrf())
                        .param("post", postID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value("Comment ID"))
                .andExpect(jsonPath("$.data.content").value("Content of comment"));
    }

    @Test
    @DisplayName("Create comment invalid")
    @Order(19)
    void createCommentInvalid() throws Exception {
        CommentDTO invalidComment = new CommentDTO();
        invalidComment.setId("Invalid comment ID");
        invalidComment.setContent("");

        mvc.perform(post("/comment")
                        .with(csrf())
                        .param("post", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("406"))
                .andExpect(jsonPath("$.message", containsString("Field error")));
    }

    @Test
    @DisplayName("Edit comment success")
    @Order(20)
    void editCommentSuccess() throws Exception {
        String username = user.getUsername();
        given(commentService.editComment(any(), eq(username))).willReturn(comment);

        mvc.perform(put("/comment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value("Comment ID"))
                .andExpect(jsonPath("$.data.content").value("Content of comment"));
    }

    @Test
    @DisplayName("Edit comment invalid")
    @Order(21)
    void editCommentInvalid() throws Exception {
        CommentDTO invalidComment = new CommentDTO();
        invalidComment.setId("Invalid comment ID");
        invalidComment.setContent("");

        mvc.perform(put("/comment")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidComment)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.code").value("406"))
                .andExpect(jsonPath("$.message", containsString("Field error")));
    }

    @Test
    @DisplayName("Delete comment")
    @Order(22)
    void deleteComment() throws Exception {
        String id = comment.getId();
        given(commentService.deleteComment(id, "user", false)).willReturn(comment);

        mvc.perform(delete("/comment/{id}", id).with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.data.id").value("Comment ID"))
                .andExpect(jsonPath("$.data.content").value("Content of comment"));
    }
}
