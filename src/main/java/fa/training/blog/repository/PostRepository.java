package fa.training.blog.repository;

import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, String> {
    List<Post> findByOwner(User owner, Pageable pageable);
    List<Post> findByCreateDateBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Post> findByTitleIgnoreCaseContains(String title, Pageable pageable);
}
