package biz.itehnika.homeaccrest.exceptions;

import lombok.Data;

import java.util.Date;

@Data
public class AppError {
//    private int status;
    private String message;
    private Date timestamp;

    public AppError(String message) {
//        this.status = status;
        this.message = message;
        this.timestamp = new Date();
    }
}
