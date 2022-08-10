package fa.training.blog.model;

public class ResponseObject {
    private String code;
    private String message;
    private Object data;

    public ResponseObject(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseObject(Object data) {
        this.code = "200";
        this.message = "Success";
        this.data = data;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
