package fa.training.blog.controller;

import fa.training.blog.model.ResponseObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping
    public ResponseObject hello() {
        return new ResponseObject("Hello Spring Boot Restful API");
    }
}
