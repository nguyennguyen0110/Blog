package fa.training.blog.service;

import fa.training.blog.dto.CommentDTO;
import fa.training.blog.dto.PostDTO;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.entity.Comment;
import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.repository.CommentRepository;
import fa.training.blog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
    @InjectMocks
    private CommentServiceImpl commentService;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private UserService userService;
    @Mock
    private PostService postService;
    @Spy
    private ModelMapper modelMapper;

    private static CommentDTO commentDTO;
    private static CommentDTO commentDTOCreate;
    private static Pageable pageable;
    private static PostDTO postDTO;
    private static UserDTO userDTO;
    private static List<CommentDTO> commentDTOS;
    private static List<Comment> comments;
    private Comment comment;

    @BeforeAll
    public static void setup() {
        postDTO = new PostDTO();
        postDTO.setTitle("Title of post");
        postDTO.setId("Post ID");

        userDTO = new UserDTO();
        userDTO.setUsername("user");
        userDTO.setFirstName("De");
        userDTO.setLastName("Mo");

        commentDTO = new CommentDTO();
        commentDTO.setId("Comment ID");
        commentDTO.setContent("This comment for test");
        commentDTO.setPost(postDTO);
        commentDTO.setOwner(userDTO);
        commentDTO.setDeleted(false);

        commentDTOS = new ArrayList<>();
        commentDTOS.add(commentDTO);

        commentDTOCreate = new CommentDTO();
        commentDTO.setContent("This comment for test");

        pageable = PageRequest.of(0, 10);

        comments = new ArrayList<>();
    }

    @BeforeEach
    public void init() {
        comment = modelMapper.map(commentDTO, Comment.class);
        comments.add(comment);
    }

    @AfterEach
    public void teardown() {
        comments.clear();
    }

    @Test
    @DisplayName("Create comment success")
    @Order(1)
    void createCommentSuccess() {
        // Given
        String postID = postDTO.getId();
        String username = userDTO.getUsername();
        given(postService.findPostByID(postID)).willReturn(postDTO);
        given(userService.findUserByUsername(username)).willReturn(userDTO);
        given(commentRepository.save(any(Comment.class))).willReturn(comment);

        // when
        CommentDTO actual = commentService.createComment(commentDTOCreate, postID, username);

        // then
        assertEquals(commentDTO.getId(), actual.getId());
    }

    @Test
    @DisplayName("Create comment throw exception post not found")
    @Order(2)
    void createCommentPostNotFound() {
        // Given
        String postID = postDTO.getId();
        given(postService.findPostByID(postID)).willReturn(null);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.createComment(commentDTOCreate, postID, userDTO.getUsername()));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    @DisplayName("Create comment throw exception username not found")
    @Order(3)
    void createCommentUsernameNotFound() {
        // Given
        String username = userDTO.getUsername();
        String postID = postDTO.getId();
        given(userService.findUserByUsername(username)).willReturn(null);
        given(postService.findPostByID(postID)).willReturn(postDTO);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.createComment(commentDTOCreate, postID, username));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Username not found", exception.getMessage());
    }

    @Test
    @DisplayName("Edit comment success")
    @Order(4)
    void editCommentSuccess() {
        // Given
        Comment editedComment = modelMapper.map(commentDTO, Comment.class);
        editedComment.setContent("This comment edited");
        given(commentRepository.findByIdAndDeleted(commentDTO.getId(), false)).willReturn(comment);
        given(commentRepository.saveAndFlush(any(Comment.class))).willReturn(editedComment);

        // when
        CommentDTO actual = commentService.editComment(commentDTO, userDTO.getUsername());

        // then
        assertEquals(commentDTO.getId(), actual.getId());
        assertNotEquals(commentDTO.getContent(), actual.getContent());
        assertEquals("This comment edited", actual.getContent());
    }

    @Test
    @DisplayName("Edit comment throw exception comment not found")
    @Order(5)
    void editCommentThrowCommentNotFound() {
        // Given
        given(commentRepository.findByIdAndDeleted(commentDTO.getId(), false)).willReturn(null);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.editComment(commentDTO, userDTO.getUsername()));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    @DisplayName("Edit comment throw exception just owner can edit")
    @Order(6)
    void editCommentNotOwner() {
        // Given
        given(commentRepository.findByIdAndDeleted(commentDTO.getId(), false)).willReturn(comment);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.editComment(commentDTO, "not owner"));

        // then
        assertEquals("403", exception.getCode());
        assertEquals("Just owner can edit comment", exception.getMessage());
    }

    @Test
    @DisplayName("Soft delete comment by owner success")
    @Order(7)
    void softDeleteCommentOwnerSuccess() {
        // Given
        String commentID = commentDTO.getId();
        Comment deletedComment = modelMapper.map(commentDTO, Comment.class);
        deletedComment.setDeleted(true);
        given(commentRepository.findByIdAndDeleted(commentID, false)).willReturn(comment);
        given(commentRepository.saveAndFlush(any(Comment.class))).willReturn(deletedComment);

        // when
        CommentDTO actual = commentService.deleteComment(commentID, userDTO.getUsername(), false);

        // then
        assertEquals(commentDTO.getId(), actual.getId());
        assertTrue(actual.isDeleted());
    }

    @Test
    @DisplayName("Soft delete comment by admin success")
    @Order(8)
    void softDeleteCommentAdminSuccess() {
        // Given
        String commentID = commentDTO.getId();
        Comment deletedComment = modelMapper.map(commentDTO, Comment.class);
        deletedComment.setContent("Deleted by admin");
        given(commentRepository.findById(commentID)).willReturn(Optional.of(comment));
        given(commentRepository.saveAndFlush(any(Comment.class))).willReturn(deletedComment);

        // when
        CommentDTO actual = commentService.deleteComment(commentID, "not owner", true);

        // then
        assertEquals(commentDTO.getId(), actual.getId());
        assertEquals("Deleted by admin", actual.getContent());
    }

    @Test
    @DisplayName("Delete comment by owner admin success")
    @Order(8)
    void deleteCommentOwnerAdminSuccess() {
        // Given
        String commentID = commentDTO.getId();
        Comment deletedComment = modelMapper.map(commentDTO, Comment.class);
        deletedComment.setDeleted(true);
        given(commentRepository.findById(commentID)).willReturn(Optional.of(deletedComment));

        // when
        CommentDTO actual = commentService.deleteComment(commentID, userDTO.getUsername(), true);

        // then
        assertEquals(commentDTO.getId(), actual.getId());
        assertTrue(actual.isDeleted());
        assertEquals("This comment for test", actual.getContent());
    }

    @Test
    @DisplayName("Delete comment by admin success")
    @Order(9)
    void deleteCommentAdminSuccess() {
        // Given
        String commentID = commentDTO.getId();
        Comment deletedComment = modelMapper.map(commentDTO, Comment.class);
        deletedComment.setContent("Deleted by admin");
        given(commentRepository.findById(commentID)).willReturn(Optional.of(deletedComment));

        // when
        CommentDTO actual = commentService.deleteComment(commentID, "not owner", true);

        // then
        assertEquals(commentDTO.getId(), actual.getId());
        assertEquals("Deleted by admin", actual.getContent());
        assertFalse(actual.isDeleted());
    }

    @Test
    @DisplayName("Delete comment throw exception comment not found")
    @Order(10)
    void deleteCommentThrowCommentNotFound() {
        // Given
        String commentID = commentDTO.getId();
        given(commentRepository.findById(commentID)).willReturn(Optional.empty());

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.deleteComment(commentID, "not owner", true));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Comment not found", exception.getMessage());
    }

    @Test
    @DisplayName("Delete comment throw exception deny")
    @Order(11)
    void deleteCommentDeny() {
        // Given
        String commentID = commentDTO.getId();
        given(commentRepository.findByIdAndDeleted(commentID, false)).willReturn(comment);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.deleteComment(commentID, "not owner", false));

        // then
        assertEquals("403", exception.getCode());
        assertEquals("Just owner or admin can delete comment", exception.getMessage());
    }

    @Test
    @DisplayName("Find all comments")
    @Order(12)
    void findAllComment() {
        // Given
        Page<Comment> page = new PageImpl<>(comments);
        given(commentRepository.findAll(pageable)).willReturn(page);

        // when
        List<CommentDTO> actual = commentService.findAllComment(pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getClass(), actual.get(0).getClass());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find !soft-deleted comments")
    @Order(13)
    void findCommentByDeleted() {
        // Given
        given(commentRepository.findByDeleted(false, pageable)).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByDeleted(pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find comments by owner success")
    @Order(13)
    void findCommentByOwnerSuccess() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(userDTO);
        given(commentRepository.findByOwner(any(User.class), any())).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByOwner(username, pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find comments by owner username not found")
    @Order(14)
    void findCommentByOwnerUsernameNotFound() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(null);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.findCommentByOwner(username, pageable));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Username not found", exception.getMessage());
    }

    @Test
    @DisplayName("Find !soft-deleted comments by owner success")
    @Order(15)
    void findCommentByOwnerAndDeletedSuccess() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(userDTO);
        given(commentRepository.findByOwnerAndDeleted(any(User.class), eq(false), any())).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByOwnerAndDeleted(username, pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find !soft-deleted comments by owner username not found")
    @Order(16)
    void findCommentByOwnerAndDeletedUsernameNotFound() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(null);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.findCommentByOwnerAndDeleted(username, pageable));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Username not found", exception.getMessage());
    }

    @Test
    @DisplayName("Find comments by post success")
    @Order(17)
    void findCommentByPostSuccess() {
        // Given
        String postID = postDTO.getId();
        given(postService.findPostByID(postID)).willReturn(postDTO);
        given(commentRepository.findByPost(any(Post.class), any())).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByPost(postID, pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find comments by post throw post not found")
    @Order(18)
    void findCommentByPostThrowPostNotFound() {
        // Given
        String postID = postDTO.getId();
        given(postService.findPostByID(postID)).willReturn(null);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.findCommentByPost(postID, pageable));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    @DisplayName("Find !soft-deleted comments by post success")
    @Order(19)
    void findCommentByPostAndDeletedSuccess() {
        // Given
        String postID = postDTO.getId();
        given(postService.findPostByID(postID)).willReturn(postDTO);
        given(commentRepository.findByPostAndDeleted(any(Post.class), eq(false), any())).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByPostAndDeleted(postID, pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find !soft-deleted comments by post throw exception post not found")
    @Order(20)
    void findCommentByPostAndDeletedPostNotFound() {
        // Given
        String postID = postDTO.getId();
        given(postService.findPostByID(postID)).willReturn(null);

        // when
        MyException exception = assertThrows(MyException.class, () -> commentService.findCommentByPostAndDeleted(postID, pageable));

        // then
        assertEquals("400", exception.getCode());
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    @DisplayName("Find comments by create date")
    @Order(21)
    void findCommentByCreateDate() {
        // Given
        given(commentRepository.findByCreateDateBetween(any(), any(), any())).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByCreateDate(LocalDate.now(), pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }

    @Test
    @DisplayName("Find !soft-deleted comments by create date")
    @Order(21)
    void findCommentByCreateDateAndDeleted() {
        // Given
        given(commentRepository.findByDeletedAndCreateDateBetween(eq(false), any(), any(), any())).willReturn(comments);

        // when
        List<CommentDTO> actual = commentService.findCommentByCreateDateAndDeleted(LocalDate.now(), pageable);

        // then
        assertEquals(commentDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(commentDTOS.get(0).getContent(), actual.get(0).getContent());
        assertEquals(commentDTOS.size(), actual.size());
    }
}
