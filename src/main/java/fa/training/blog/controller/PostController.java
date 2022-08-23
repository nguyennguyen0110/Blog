package fa.training.blog.controller;

import fa.training.blog.dto.PostDTO;
import fa.training.blog.model.ResponseObject;
import fa.training.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/post")
public class PostController {
    @Autowired
    private PostService postService;

    @GetMapping
    public ResponseObject findPost(@RequestParam(required = false) String id,
                                   @RequestParam(required = false) String owner,
                                   @RequestParam(required = false) String createDate,
                                   @RequestParam(required = false) String title,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createDate"));
        if (id != null){
            PostDTO post = postService.findPostByID(id);
            if (post != null) {
                return new ResponseObject(post);
            } else {
                return new ResponseObject("400", "Post not found");
            }
        } else if (owner != null) {
            return new ResponseObject(postService.findPostByOwner(owner, pageable));
        } else if (createDate != null) {
            LocalDate create = LocalDate.parse(createDate);
            return new ResponseObject(postService.findPostByCreateDate(create, pageable));
        } else if (title != null) {
            return new ResponseObject(postService.findPostByTitle(title, pageable));
        } else {
            return new ResponseObject(postService.findAllPost(pageable));
        }
    }

    @PostMapping
    public ResponseObject createPost(@RequestBody @Valid PostDTO postDTO, BindingResult result){
        if(!result.hasErrors()){
            return new ResponseObject(postService.createPost(postDTO));
        }
        else {
            return new ResponseObject("400", result.getFieldError().toString());
        }
    }

    @PutMapping
    public ResponseObject editPost(@RequestBody @Valid PostDTO postDTO, BindingResult result){
        if(!result.hasErrors()){
            return new ResponseObject(postService.editPost(postDTO));
        }
        else {
            return new ResponseObject("400", result.getFieldError().toString());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseObject deletePost(@PathVariable("id") String id){
        return new ResponseObject(postService.deletePost(id));
    }
}
