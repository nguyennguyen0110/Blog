package fa.training.blog.controller;

import fa.training.blog.entity.User;
import fa.training.blog.exception.MyException;
import fa.training.blog.model.ResponseObject;
import fa.training.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseObject findAll(){
        return new ResponseObject(userService.findAllUser());
    }

    @GetMapping(value = "/{username}")
    public ResponseObject findByUsername(@PathVariable("username") String username){
        User user = userService.findUserById(username);
        if(user != null){
            return new ResponseObject(user);
        }
        else {
            return new ResponseObject("400", "Username not found");
        }
    }

    @PostMapping
    public ResponseObject createUser(@RequestBody @Valid User user, BindingResult result){
        if(!result.hasErrors()){
            return new ResponseObject(userService.createUser(user));
        }
        else {
            throw new MyException("400", result.getFieldError().toString());
        }
    }

    @PutMapping
    public ResponseObject editUser(@RequestBody @Valid User user, BindingResult result){
        if(!result.hasErrors()){
            return new ResponseObject(userService.editUser(user));
        }
        else {
            throw new MyException("400", result.getFieldError().toString());
        }
    }

    @DeleteMapping(value = "/{username}")
    public ResponseObject deleteUser(@PathVariable("username") String username){
        User deletedUser = userService.deleteUserById(username);
        if(deletedUser != null){
            return new ResponseObject(deletedUser);
        }
        else {
            return new ResponseObject("400", "Username not found");
        }
    }
}
