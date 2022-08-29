package fa.training.blog.service.impl;

import fa.training.blog.dto.PostDTO;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.repository.PostRepository;
import fa.training.blog.service.PostService;
import fa.training.blog.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PostDTO createPost(PostDTO postDTO, String username) {
        // Set owner
        UserDTO owner = userService.findUserByUsername(username);
        if (owner == null) {
            throw new MyException("400", "Username not found");
        }
        postDTO.setOwner(owner);

        // Set ID using UUID
        UUID id = UUID.randomUUID();
        postDTO.setId(id.toString());

        // Set view and modify date
        postDTO.setModifyDate(LocalDateTime.now());
        postDTO.setView(0);

        Post savedPost = postRepository.save(modelMapper.map(postDTO, Post.class));
        return modelMapper.map(savedPost, PostDTO.class);
    }

    @Override
    public PostDTO editPost(PostDTO postDTO) {
        PostDTO postToEdit = findPostByID(postDTO.getId());
        if (postToEdit != null){
            postToEdit.setModifyDate(LocalDateTime.now());
            postToEdit.setContent(postDTO.getContent());
            postToEdit.setTitle(postToEdit.getTitle());
            Post editedPost = postRepository.saveAndFlush(modelMapper.map(postToEdit, Post.class));
            return modelMapper.map(editedPost, PostDTO.class);
        } else {
            throw new MyException("400", "Post not found");
        }
    }

    @Override
    public PostDTO deletePost(String id) {
        PostDTO postToDelete = findPostByID(id);
        if (postToDelete != null){
            postRepository.deleteById(id);
            return postToDelete;
        }else {
            throw new MyException("400", "Post not found");
        }
    }

    @Override
    public List<PostDTO> findAllPost(Pageable pageable) {
        Page<Post> posts = postRepository.findAll(pageable);
        return posts.stream().map(post -> modelMapper.map(post, PostDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> findPostByOwner(String username, Pageable pageable) {
        UserDTO owner = userService.findUserByUsername(username);
        if (owner == null) {
            throw new MyException("400", "Username not found");
        }
        List<Post> posts = postRepository.findByOwner(modelMapper.map(owner, User.class), pageable);
        return posts.stream().map(post -> modelMapper.map(post, PostDTO.class)).collect(Collectors.toList());
    }

    @Override
    public PostDTO findPostByID(String id) {
        Optional<Post> post = postRepository.findById(id);
        if (post.isPresent()) {
            Post postFromDB = post.get();
            // Update post has one more view
            postFromDB.setView(postFromDB.getView() + 1);
            PostDTO responsePost = modelMapper.map(postFromDB, PostDTO.class);
            postRepository.saveAndFlush(postFromDB);
            return responsePost;
        } else {
            return null;
        }
    }

    @Override
    public List<PostDTO> findPostByCreateDate(LocalDate createDate, Pageable pageable) {
        LocalDateTime start = createDate.atStartOfDay();
        LocalDateTime end = createDate.atTime(LocalTime.MAX);
        List<Post> posts = postRepository.findByCreateDateBetween(start, end, pageable);
        return posts.stream().map(post -> modelMapper.map(post, PostDTO.class)).collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> findPostByTitle(String title, Pageable pageable) {
        List<Post> posts = postRepository.findByTitleIgnoreCaseContains(title, pageable);
        return posts.stream().map(post -> modelMapper.map(post, PostDTO.class)).collect(Collectors.toList());
    }
}
