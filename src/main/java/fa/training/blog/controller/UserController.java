package fa.training.blog.controller;

import fa.training.blog.dto.UserDTO;
import fa.training.blog.model.ResponseObject;
import fa.training.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseObject findUser(@RequestParam(required = false) String username,
                                   @RequestParam(required = false) String firstName,
                                   @RequestParam(required = false) String lastName,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by("role").and(Sort.by("username")));
        if (username != null){
            UserDTO user = userService.findUserByUsername(username);
            if(user != null){
                return new ResponseObject(user);
            }
            else {
                return new ResponseObject("400", "Username not found");
            }
        } else if (firstName != null && lastName != null) {
            List<UserDTO> users = userService.findUserByFirstNameAndLastName(firstName, lastName, pageable);
            return new ResponseObject(users);
        } else if (firstName != null) {
            List<UserDTO> users = userService.findUserByFirstName(firstName, pageable);
            return new ResponseObject(users);
        } else if (lastName != null) {
            List<UserDTO> users = userService.findUserByLastName(lastName, pageable);
            return new ResponseObject(users);
        } else {
            List<UserDTO> users = userService.findAllUser(pageable);
            return new ResponseObject(users);
        }

    }

    @PutMapping
    public ResponseObject editUser(@RequestBody @Valid UserDTO userDTO, BindingResult result){
        if(!result.hasErrors()){
            return new ResponseObject(userService.editUser(userDTO));
        }
        else {
            return new ResponseObject("406", result.getFieldError().toString());
        }
    }

    @DeleteMapping("/{username}")
    public ResponseObject deleteUser(@PathVariable("username") String username){
        return new ResponseObject(userService.deleteUserByUsername(username));
    }
}
