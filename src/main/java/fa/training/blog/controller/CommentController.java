package fa.training.blog.controller;

import fa.training.blog.dto.CommentDTO;
import fa.training.blog.model.ResponseObject;
import fa.training.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseObject findComment(@RequestParam(required = false) String id,
                                      @RequestParam(required = false) String owner,
                                      @RequestParam(required = false) String post,
                                      @RequestParam(required = false) String createDate,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size,
                                            Sort.by("post")
                                                    .and(Sort.by(Sort.Direction.DESC, "createDate")));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // If anonymous user get comment then return comment that not soft-deleted
        if (authentication instanceof AnonymousAuthenticationToken) {
            if (id != null) {
                CommentDTO commentDTO = commentService.findCommentByIDAndDeleted(id);
                if (commentDTO != null) {
                    return new ResponseObject(commentDTO);
                } else {
                    return new ResponseObject("400", "Comment not found");
                }
            } else if (owner != null) {
                return new ResponseObject(commentService.findCommentByOwnerAndDeleted(owner, pageable));
            } else if (post != null) {
                return new ResponseObject(commentService.findCommentByPostAndDeleted(post, pageable));
            } else if (createDate != null) {
                LocalDate create = LocalDate.parse(createDate);
                return new ResponseObject(commentService.findCommentByCreateDateAndDeleted(create, pageable));
            } else {
                return new ResponseObject(commentService.findCommentByDeleted(pageable));
            }
        }
        // else if an authenticated user signed in:
        // if ROLE_ADMIN then return comments include soft-deleted, else just return not soft-deleted comments
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        boolean isAdmin = "ROLE_ADMIN".equals(authority.get().getAuthority());
        if (id != null) {
            CommentDTO commentDTO;
            if (isAdmin) {
                commentDTO = commentService.findCommentByID(id);
            } else {
                commentDTO = commentService.findCommentByIDAndDeleted(id);
            }
            if (commentDTO != null) {
                return new ResponseObject(commentDTO);
            }
            return new ResponseObject("400", "Comment not found");
        } else if (owner != null) {
            if (isAdmin) {
                return new ResponseObject(commentService.findCommentByOwner(owner, pageable));
            }
            return new ResponseObject(commentService.findCommentByOwnerAndDeleted(owner, pageable));
        } else if (post != null) {
            if (isAdmin) {
                return new ResponseObject(commentService.findCommentByPost(post, pageable));
            }
            return new ResponseObject(commentService.findCommentByPostAndDeleted(post, pageable));
        } else if (createDate != null) {
            LocalDate create = LocalDate.parse(createDate);
            if (isAdmin) {
                return new ResponseObject(commentService.findCommentByCreateDate(create, pageable));
            }
            return new ResponseObject(commentService.findCommentByCreateDateAndDeleted(create, pageable));
        } else {
            if (isAdmin) {
                return new ResponseObject(commentService.findAllComment(pageable));
            }
            return new ResponseObject(commentService.findCommentByDeleted(pageable));
        }
    }

    @PostMapping
    // ************************* BindingResult MUST come RIGHT AFTER @Valid parameter *************************
    public ResponseObject createComment(@RequestBody @Valid CommentDTO commentDTO, BindingResult result,
                                        @RequestParam String post) {
        if (!result.hasErrors()) {
            // User creating comment is owner
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            return new ResponseObject(commentService.createComment(commentDTO, post, username));
        } else {
            return new ResponseObject("406", result.getFieldError().toString());
        }
    }

    @PutMapping
    public ResponseObject editComment(@RequestBody @Valid CommentDTO commentDTO, BindingResult result) {
        if (!result.hasErrors()) {
            // Get editing user to check if also is owner, because just owner can edit comment
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            return new ResponseObject(commentService.editComment(commentDTO, username));
        } else {
            return new ResponseObject("406", result.getFieldError().toString());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseObject deleteComment(@PathVariable("id") String id) {
        // Get user deleting comment to check if is owner
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        // and check if user deleting is admin
        Optional<? extends GrantedAuthority> authority = authentication.getAuthorities().stream().findFirst();
        boolean isAdmin = "ROLE_ADMIN".equals(authority.get().getAuthority());
        return new ResponseObject(commentService.deleteComment(id, username, isAdmin));
    }
}
