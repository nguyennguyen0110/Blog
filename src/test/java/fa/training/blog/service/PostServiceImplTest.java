package fa.training.blog.service;

import fa.training.blog.dto.PostDTO;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.repository.PostRepository;
import fa.training.blog.service.impl.PostServiceImpl;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class PostServiceImplTest {
    @InjectMocks
    private PostServiceImpl postService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserService userService;
    @Spy
    private ModelMapper modelMapper;

    private static PostDTO postDTO;
    private static PostDTO postDTOCreate;
    private static Pageable pageable;
    private static UserDTO userDTO;
    private static List<PostDTO> postDTOS;
    private Post post;
    private List<Post> posts;

    @BeforeAll
    static void setup() {
        userDTO = new UserDTO();
        userDTO.setUsername("user");
        userDTO.setFirstName("De");
        userDTO.setLastName("Mo");

        postDTO = new PostDTO();
        postDTO.setId("Post ID");
        postDTO.setTitle("Title of post");
        postDTO.setContent("Content of post");
        postDTO.setView(0);
        postDTO.setOwner(userDTO);

        postDTOS = new ArrayList<>();
        postDTOS.add(postDTO);

        postDTOCreate= new PostDTO();
        postDTOCreate.setTitle("Title of post");
        postDTOCreate.setContent("Content of post");

        pageable = PageRequest.of(0, 10);
    }

    @BeforeEach
    void setUp() {
        post = modelMapper.map(postDTO, Post.class);
        posts = new ArrayList<>();
        posts.add(post);
    }

    @AfterEach
    void tearDown() {
        posts.clear();
    }

    @Test
    @DisplayName("Create post success")
    @Order(1)
    void createPostSuccess() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(userDTO);
        given(postRepository.save(any(Post.class))).willReturn(post);

        // When
        PostDTO actual = postService.createPost(postDTOCreate, username);

        // Then
        assertEquals(postDTO.getId(), actual.getId());
        assertEquals(postDTO.getTitle(), actual.getTitle());
        assertEquals(postDTO.getContent(), actual.getContent());
    }

    @Test
    @DisplayName("Create post username not found")
    @Order(2)
    void createPostUsernameNotFound() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(null);

        // When
        MyException exception = assertThrows(MyException.class, () -> postService.createPost(postDTOCreate, username));

        // Then
        assertEquals("400", exception.getCode());
        assertEquals("Username not found", exception.getMessage());
    }

    @Test
    @DisplayName("Edit post success")
    @Order(3)
    void editPostSuccess() {
        // Given
        given(postRepository.findById(postDTO.getId())).willReturn(Optional.of(post));
        Post editedPost = modelMapper.map(postDTO, Post.class);
        editedPost.setTitle("Edited title of post");
        editedPost.setContent("Edited content of post");
        given(postRepository.saveAndFlush(any(Post.class))).willReturn(editedPost);

        // When
        PostDTO actual = postService.editPost(postDTO);

        // Then
        assertEquals(postDTO.getId(), actual.getId());
        assertEquals("Edited title of post", actual.getTitle());
        assertEquals("Edited content of post", actual.getContent());
    }

    @Test
    @DisplayName("Edit post throw post not found")
    @Order(4)
    void editPostThrowPostNotFound() {
        // Given
        given(postRepository.findById(postDTO.getId())).willReturn(Optional.empty());

        // When
        MyException exception = assertThrows(MyException.class, () -> postService.editPost(postDTO));

        // Then
        assertEquals("400", exception.getCode());
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    @DisplayName("Delete post success")
    @Order(5)
    void deletePostSuccess() {
        // Given
        String postID = postDTO.getId();
        given(postRepository.findById(postID)).willReturn(Optional.of(post));

        // When
        PostDTO actual = postService.deletePost(postID);

        // Then
        assertEquals(postDTO.getId(), actual.getId());
        assertEquals(postDTO.getTitle(), actual.getTitle());
        assertEquals(postDTO.getContent(), actual.getContent());
    }

    @Test
    @DisplayName("Delete post throw post not found")
    @Order(6)
    void deletePostThrowPostNotFound() {
        // Given
        String postID = postDTO.getId();
        given(postRepository.findById(postID)).willReturn(Optional.empty());

        // When
        MyException exception = assertThrows(MyException.class, () -> postService.deletePost(postID));

        // Then
        assertEquals("400", exception.getCode());
        assertEquals("Post not found", exception.getMessage());
    }

    @Test
    @DisplayName("Find all post")
    @Order(7)
    void findAllPost() {
        // Given
        Page<Post> page = new PageImpl<>(posts);
        given(postRepository.findAll(pageable)).willReturn(page);

        // When
        List<PostDTO> actual = postService.findAllPost(pageable);

        // Then
        assertEquals(postDTOS.size(), actual.size());
        assertEquals(postDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(postDTOS.get(0).getTitle(), actual.get(0).getTitle());
    }

    @Test
    @DisplayName("Find post by owner")
    @Order(8)
    void findPostByOwner() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(userDTO);
        given(postRepository.findByOwner(any(User.class), any())).willReturn(posts);

        // When
        List<PostDTO> actual = postService.findPostByOwner(username, pageable);

        // Then
        assertEquals(postDTOS.size(), actual.size());
        assertEquals(postDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(postDTOS.get(0).getTitle(), actual.get(0).getTitle());
    }

    @Test
    @DisplayName("Find post by owner username not found")
    @Order(9)
    void findPostByOwnerUsernameNotFound() {
        // Given
        String username = userDTO.getUsername();
        given(userService.findUserByUsername(username)).willReturn(null);

        // When
        MyException exception = assertThrows(MyException.class, () -> postService.findPostByOwner(username, pageable));

        // Then
        assertEquals("400", exception.getCode());
        assertEquals("Username not found", exception.getMessage());
    }

    @Test
    @DisplayName("Find post by create date")
    @Order(10)
    void findPostByCreateDate() {
        // Given
        given(postRepository.findByCreateDateBetween(any(), any(), any())).willReturn(posts);

        // When
        List<PostDTO> actual = postService.findPostByCreateDate(LocalDate.now(), pageable);

        // Then
        assertEquals(postDTOS.size(), actual.size());
        assertEquals(postDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(postDTOS.get(0).getTitle(), actual.get(0).getTitle());
    }

    @Test
    @DisplayName("Find post by title")
    @Order(11)
    void findPostByTitle() {
        // Given
        String title = postDTO.getTitle();
        given(postRepository.findByTitleIgnoreCaseContains(title, pageable)).willReturn(posts);

        // When
        List<PostDTO> actual = postService.findPostByTitle(title, pageable);

        // Then
        assertEquals(postDTOS.size(), actual.size());
        assertEquals(postDTOS.get(0).getId(), actual.get(0).getId());
        assertEquals(postDTOS.get(0).getTitle(), actual.get(0).getTitle());
    }
}
