package fa.training.blog.service;

import fa.training.blog.dto.PostDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PostService {
    public PostDTO createPost(PostDTO postDTO);
    public PostDTO editPost(PostDTO postDTO);
    public PostDTO deletePost(String id);
    public List<PostDTO> findAllPost(Pageable pageable);
    public List<PostDTO> findPostByOwner(String username, Pageable pageable);
    public PostDTO findPostByID(String id);
    public List<PostDTO> findPostByCreateDate(LocalDate createDate, Pageable pageable);
    public List<PostDTO> findPostByTitle(String title, Pageable pageable);
}
