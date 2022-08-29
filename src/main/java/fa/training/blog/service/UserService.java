package fa.training.blog.service;

import fa.training.blog.dto.UserDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    /**
     * Create and save user to database
     * @param user to create
     * @return user saved to database
     */
    UserDTO createUser(UserDTO user);

    /**
     * Delete a user from database
     * @param username to delete
     * @return deleted user
     */
    UserDTO deleteUserByUsername(String username);

    /**
     * Update a user in database
     * @param user with update
     * @return updated user
     */
    UserDTO editUser(UserDTO user);

    /**
     * Find all user in database
     * @param pageable a Pageable object
     * @return list of user found or empty list if not found any
     */
    List<UserDTO> findAllUser(Pageable pageable);

    /**
     * Find user in database by username
     * @param username to find
     * @return user found else null
     */
    UserDTO findUserByUsername(String username);

    /**
     * Find user in database by email
     * @param email of user
     * @return user found else null
     */
    UserDTO findUserByEmail(String email);

    /**
     * Find user in database by first name
     * @param firstName of user
     * @param pageable a Pageable object
     * @return list of user found or empty list if not found any
     */
    List<UserDTO> findUserByFirstName(String firstName, Pageable pageable);

    /**
     * Find user in database by last name
     * @param lastName of user
     * @param pageable a Pageable object
     * @return list of user found or empty list if not found any
     */
    List<UserDTO> findUserByLastName(String lastName, Pageable pageable);

    /**
     * Find user in database by first name and last name
     * @param firstName of user
     * @param lastName of user
     * @param pageable a Pageable object
     * @return list of user found or empty list if not found any
     */
    List<UserDTO> findUserByFirstNameAndLastName(String firstName, String lastName, Pageable pageable);
}
