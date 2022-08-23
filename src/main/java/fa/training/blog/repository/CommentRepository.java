package fa.training.blog.repository;

import fa.training.blog.entity.Comment;
import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByOwner(User owner, Pageable pageable);
    List<Comment> findByPost(Post post, Pageable pageable);
    List<Comment> findByCreateDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    Comment findByIdAndDeleted(String id, boolean deleted);
    List<Comment> findByOwnerAndDeleted(User owner, boolean deleted, Pageable pageable);
    List<Comment> findByPostAndDeleted(Post post, boolean deleted, Pageable pageable);
    List<Comment> findByDeletedAndCreateDateBetween(boolean deleted, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Comment> findByDeleted(boolean deleted, Pageable pageable);
}
