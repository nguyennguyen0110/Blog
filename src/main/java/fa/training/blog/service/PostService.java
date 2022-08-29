package fa.training.blog.service;

import fa.training.blog.dto.PostDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PostService {
    /**
     * Create post and save to database
     * @param postDTO post to create (just title and content)
     * @param username of post's owner
     * @return post saved to database
     */
    PostDTO createPost(PostDTO postDTO, String username);

    /**
     * Update post in database
     * @param postDTO post with update
     * @return post updated to database
     */
    PostDTO editPost(PostDTO postDTO);

    /**
     * Delete post from database
     * @param id of post going to delete
     * @return deleted post
     */
    PostDTO deletePost(String id);

    /**
     * Find all post in database
     * @param pageable a Pageable object
     * @return list of post found or empty list if not found any
     */
    List<PostDTO> findAllPost(Pageable pageable);

    /**
     * Find post in database by owner
     * @param username of owner
     * @param pageable a Pageable object
     * @return list of post found or empty list if not found any
     */
    List<PostDTO> findPostByOwner(String username, Pageable pageable);

    /**
     * Find post in database by ID
     * @param id of post
     * @return post found else null
     */
    PostDTO findPostByID(String id);

    /**
     * Find post in database by create date
     * @param createDate date post created
     * @param pageable a Pageable object
     * @return list of post found or empty list if not found any
     */
    List<PostDTO> findPostByCreateDate(LocalDate createDate, Pageable pageable);

    /**
     * Find post in database by title
     * @param title of post
     * @param pageable a Pageable object
     * @return list of post found or empty list if not found any
     */
    List<PostDTO> findPostByTitle(String title, Pageable pageable);
}
