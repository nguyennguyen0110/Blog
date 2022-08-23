package fa.training.blog.repository;

import fa.training.blog.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    List<User> findByFirstNameIgnoreCaseContains(String firstName, Pageable pageable);
    List<User> findByLastNameIgnoreCaseContains(String lastName, Pageable pageable);
    List<User> findByFirstNameIgnoreCaseContainsAndLastNameIgnoreCaseContains(String firstName, String lastName, Pageable pageable);
}
