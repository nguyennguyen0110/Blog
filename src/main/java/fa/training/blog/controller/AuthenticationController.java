package fa.training.blog.controller;

import fa.training.blog.dto.UserDTO;
import fa.training.blog.exception.MyException;
import fa.training.blog.model.ResponseObject;
import fa.training.blog.security.JwtTokenUtil;
import fa.training.blog.security.JwtUserDetailsService;
import fa.training.blog.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class AuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService jwtUserDetailsService;
    @Autowired
    private UserService userService;

    private void authenticate(String username, String password){
        try {
            // Authenticate sign in username and password with user in database
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new MyException("401", "User disabled");
        } catch (BadCredentialsException e) {
            throw new MyException("401", "Invalid credentials");
        }
    }

    @PostMapping("/signup")
    public ResponseObject createUser(@RequestBody @Valid UserDTO userDTO, BindingResult result){
        if(!result.hasErrors()){
            return new ResponseObject(userService.createUser(userDTO));
        }
        else {
            return new ResponseObject("406", result.getFieldError().toString());
        }
    }

    @PostMapping("/signin")
    public ResponseObject createAuthenticationToken(@RequestBody UserDTO userDTO){
        // First authenticate sign in information with user in database
        authenticate(userDTO.getUsername(), userDTO.getPassword());

        // then generate token and return
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(userDTO.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        return new ResponseObject(token);
    }
}
