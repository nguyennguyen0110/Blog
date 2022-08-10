package fa.training.blog.exception.handler;

import fa.training.blog.exception.MyException;
import fa.training.blog.model.ResponseObject;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class MyExceptionHandler {
    @ExceptionHandler(MyException.class)
    @ResponseBody
    public ResponseObject handleMyException(MyException e){
        return new ResponseObject(e.getErrCode(), e.getErrMsg());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseObject handleException(Exception e){
        return new ResponseObject("400", e.getMessage());
    }
}
