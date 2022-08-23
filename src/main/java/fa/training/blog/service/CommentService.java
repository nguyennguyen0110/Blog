package fa.training.blog.service;

import fa.training.blog.dto.CommentDTO;
import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface CommentService {
    public CommentDTO createComment(CommentDTO commentDTO, String postID);
    public CommentDTO editComment(CommentDTO commentDTO);
    public CommentDTO deleteComment(String id);
    public CommentDTO findCommentByID(String id);
    public List<CommentDTO> findAllComment(Pageable pageable);
    public List<CommentDTO> findCommentByOwner(String username, Pageable pageable);
    public List<CommentDTO> findCommentByPost(String postID, Pageable pageable);
    public List<CommentDTO> findCommentByCreateDate(LocalDate createDate, Pageable pageable);
}
