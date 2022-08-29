package fa.training.blog.service;

import fa.training.blog.dto.CommentDTO;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CommentService {
    /**
     * Create a comment and save to database.
     * @param commentDTO comment to create (just content)
     * @param postID ID of post that this comment belongs
     * @param username of comment's owner
     * @return comment saved to database
     */
    CommentDTO createComment(CommentDTO commentDTO, String postID, String username);

    /**
     * Update new comment to database.
     * @param commentDTO comment with new update
     * @param username of comment's owner
     * @return comment updated to database
     */
    CommentDTO editComment(CommentDTO commentDTO, String username);

    /**
     * Soft-delete comment for the first time by owner or admin.
     * Real delete from database the second time by admin only.
     * @param id of comment to delete
     * @param username who is deleting comment
     * @param isAdmin is an admin deleting comment
     * @return comment soft-deleted or real deleted from database
     */
    CommentDTO deleteComment(String id, String username, boolean isAdmin);

    /**
     * Find comment from database by ID
     * @param id of comment
     * @return comment found or null if not found
     */
    CommentDTO findCommentByID(String id);

    /**
     * Find comment from database by ID and not soft-deleted
     * @param id of comment
     * @return comment found or null if not found
     */
    CommentDTO findCommentByIDAndDeleted(String id);

    /**
     * Find all comment in database
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findAllComment(Pageable pageable);

    /**
     * Find all comment in database that is not soft-deleted
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByDeleted(Pageable pageable);

    /**
     * Find all comment in database of an owner
     * @param username of owner
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByOwner(String username, Pageable pageable);

    /**
     * Find all comment in database of an owner and not soft-deleted
     * @param username of owner
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByOwnerAndDeleted(String username, Pageable pageable);

    /**
     * Find all comment in database of a post
     * @param postID of post
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByPost(String postID, Pageable pageable);

    /**
     * Find all comment in database of a post and not soft-deleted
     * @param postID of post
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByPostAndDeleted(String postID, Pageable pageable);

    /**
     * Find all comment in database that create in a specific date
     * @param createDate date comments created
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByCreateDate(LocalDate createDate, Pageable pageable);

    /**
     * Find all comment in database that create in a specific date and not soft-deleted
     * @param createDate date comment created
     * @param pageable a Pageable object
     * @return list of comments found or empty list if not found any
     */
    List<CommentDTO> findCommentByCreateDateAndDeleted(LocalDate createDate, Pageable pageable);
}
