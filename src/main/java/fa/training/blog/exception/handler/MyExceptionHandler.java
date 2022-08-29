package fa.training.blog.exception.handler;

import fa.training.blog.exception.MyException;
import fa.training.blog.model.ResponseObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeParseException;

@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(MyException.class)
    @ResponseBody
    public ResponseObject handleMyException(MyException e){
        return new ResponseObject(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseBody
    public ResponseObject handleAuthenticationException() {
        return new ResponseObject("401", "Authentication fail");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseObject handleAccessDeniedException() {
        return new ResponseObject("403", "Access denied");
    }

    @ExceptionHandler(DateTimeParseException.class)
    @ResponseBody
    public ResponseObject handleDateTimeParseException() {
        return new ResponseObject("402", "Cannot parse date from string. Make sure date entered in ISO format: YYYY-MM-DD");
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseObject handleException(Exception e){
        return new ResponseObject("407", e.getMessage());
    }
}
