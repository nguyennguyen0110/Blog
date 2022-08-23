package fa.training.blog.controller;

import fa.training.blog.dto.CommentDTO;
import fa.training.blog.model.ResponseObject;
import fa.training.blog.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

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
        if (id != null) {
            CommentDTO commentDTO = commentService.findCommentByID(id);
            if (commentDTO != null) {
                return new ResponseObject(commentDTO);
            } else {
                return new ResponseObject("400", "Comment not found");
            }
        } else if (owner != null) {
            return new ResponseObject(commentService.findCommentByOwner(owner, pageable));
        } else if (post != null) {
            return new ResponseObject(commentService.findCommentByPost(post, pageable));
        } else if (createDate != null) {
            LocalDate create = LocalDate.parse(createDate);
            return new ResponseObject(commentService.findCommentByCreateDate(create, pageable));
        } else {
            return new ResponseObject(commentService.findAllComment(pageable));
        }
    }

    @PostMapping
    public ResponseObject createComment(@RequestBody @Valid CommentDTO commentDTO,
                                        @RequestParam String post, BindingResult result) {
        if (!result.hasErrors()) {
            return new ResponseObject(commentService.createComment(commentDTO, post));
        } else {
            return new ResponseObject("400", result.getFieldError().toString());
        }
    }

    @PutMapping
    public ResponseObject editComment(@RequestBody @Valid CommentDTO commentDTO, BindingResult result) {
        if (!result.hasErrors()) {
            return new ResponseObject(commentService.editComment(commentDTO));
        } else {
            return new ResponseObject("400", result.getFieldError().toString());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseObject deleteComment(@PathVariable("id") String id) {
        return new ResponseObject(commentService.deleteComment(id));
    }
}
