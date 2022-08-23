package fa.training.blog.service.impl;

import fa.training.blog.dto.CommentDTO;
import fa.training.blog.dto.PostDTO;
import fa.training.blog.dto.UserDTO;
import fa.training.blog.entity.Comment;
import fa.training.blog.entity.Post;
import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.repository.CommentRepository;
import fa.training.blog.service.CommentService;
import fa.training.blog.service.PostService;
import fa.training.blog.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CommentDTO createComment(CommentDTO commentDTO, String postID) {
        // Set post
        PostDTO postDTO = postService.findPostByID(postID);
        if (postDTO == null) {
            throw new MyException("400", "Post not found");
        }
        commentDTO.setPost(postDTO);

        // Set ID using UUID
        UUID id = UUID.randomUUID();
        commentDTO.setId(id.toString());

        commentDTO.setDeleted(false);

        // Set owner
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        UserDTO owner = userService.findUserByUsername(username);
        commentDTO.setOwner(owner);

        Comment savedComment = commentRepository.save(modelMapper.map(commentDTO, Comment.class));
        return modelMapper.map(savedComment, CommentDTO.class);
    }

    @Override
    public CommentDTO editComment(CommentDTO commentDTO) {
        CommentDTO commentToEdit = findCommentByID(commentDTO.getId());
        if (commentToEdit != null) {
            // Just owner can edit
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            if (!username.equals(commentToEdit.getOwner().getUsername())) {
                throw new MyException("403", "Just owner can edit comment");
            }

            commentToEdit.setContent(commentDTO.getContent());
            Comment editedComment = commentRepository.saveAndFlush(modelMapper.map(commentToEdit, Comment.class));
            return modelMapper.map(editedComment, CommentDTO.class);
        } else {
            throw new MyException("400", "Comment not found");
        }
    }

    @Override
    public CommentDTO deleteComment(String id) {
        CommentDTO commentToDelete = findCommentByID(id);
        if (commentToDelete != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
            // If user is owner (either role admin or role user)
            if (username.equals(commentToDelete.getOwner().getUsername())) {
                // If comment is soft-deleted and owner has role admin (because just admin can get soft-deleted comment)
                if (commentToDelete.isDeleted()) {
                    commentRepository.deleteById(id);
                    return commentToDelete;
                }
                // else just soft-delete the comment
                commentToDelete.setDeleted(true);
                Comment deletedComment = commentRepository.saveAndFlush(modelMapper.map(commentToDelete, Comment.class));
                return modelMapper.map(deletedComment, CommentDTO.class);
            // else if user is not owner but has role admin
            } else if ("ROLE_ADMIN".equals(authority.get().getAuthority())) {
                // If comment is soft-deleted or "Deleted by admin" then delete it for real
                if (commentToDelete.isDeleted() || "Deleted by admin".equals(commentToDelete.getContent())) {
                    commentRepository.deleteById(id);
                    return commentToDelete;
                }
                // else just change content to "Deleted by admin"
                commentToDelete.setContent("Deleted by admin");
                Comment deletedByAdmin = commentRepository.saveAndFlush(modelMapper.map(commentToDelete, Comment.class));
                return modelMapper.map(deletedByAdmin, CommentDTO.class);
            // else if user is not owner nor admin then throw exception
            } else {
                throw new MyException("403", "Just owner or admin can delete comment");
            }
        } else {
            throw new MyException("400", "Comment not found");
        }
    }

    @Override
    public CommentDTO findCommentByID(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            Comment comment = commentRepository.findByIdAndDeleted(id, false);
            if (comment == null) {
                return null;
            }
            return modelMapper.map(comment, CommentDTO.class);
        }
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        if ("ROLE_ADMIN".equals(authority.get().getAuthority())) {
            Optional<Comment> comment = commentRepository.findById(id);
            return comment.map(value -> modelMapper.map(value, CommentDTO.class)).orElse(null);
        } else {
            Comment comment = commentRepository.findByIdAndDeleted(id, false);
            if (comment == null) {
                return null;
            }
            return modelMapper.map(comment, CommentDTO.class);
        }
    }

    @Override
    public List<CommentDTO> findAllComment(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            List<Comment> comments = commentRepository.findByDeleted(false, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        if ("ROLE_ADMIN".equals(authority.get().getAuthority())) {
            Page<Comment> comments = commentRepository.findAll(pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        } else {
            List<Comment> comments = commentRepository.findByDeleted(false, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
    }

    @Override
    public List<CommentDTO> findCommentByOwner(String username, Pageable pageable) {
        UserDTO owner = userService.findUserByUsername(username);
        if (owner == null) {
            throw new MyException("400", "Username not found");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            List<Comment> comments = commentRepository.findByOwnerAndDeleted(modelMapper.map(owner, User.class), false, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        if ("ROLE_ADMIN".equals(authority.get().getAuthority())) {
            List<Comment> comments = commentRepository.findByOwner(modelMapper.map(owner, User.class), pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        } else {
            List<Comment> comments = commentRepository.findByOwnerAndDeleted(modelMapper.map(owner, User.class), false, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
    }

    @Override
    public List<CommentDTO> findCommentByPost(String postID, Pageable pageable) {
        PostDTO postDTO = postService.findPostByID(postID);
        if (postDTO == null) {
            throw new MyException("400", "Post not found");
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            List<Comment> comments = commentRepository.findByPostAndDeleted(modelMapper.map(postDTO, Post.class), false, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        if ("ROLE_ADMIN".equals(authority.get().getAuthority())) {
            List<Comment> comments = commentRepository.findByPost(modelMapper.map(postDTO, Post.class), pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        } else {
            List<Comment> comments = commentRepository.findByPostAndDeleted(modelMapper.map(postDTO, Post.class), false, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
    }

    @Override
    public List<CommentDTO> findCommentByCreateDate(LocalDate createDate, Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LocalDateTime start = createDate.atStartOfDay();
        LocalDateTime end = createDate.atTime(LocalTime.MAX);
        if (authentication instanceof AnonymousAuthenticationToken) {
            List<Comment> comments = commentRepository.findByDeletedAndCreateDateBetween(false, start, end, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        if ("ROLE_ADMIN".equals(authority.get().getAuthority())) {
            List<Comment> comments = commentRepository.findByCreateDateBetween(start, end, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        } else {
            List<Comment> comments = commentRepository.findByDeletedAndCreateDateBetween(false, start, end, pageable);
            return comments.stream().map(comment -> modelMapper.map(comment, CommentDTO.class)).collect(Collectors.toList());
        }
    }
}
