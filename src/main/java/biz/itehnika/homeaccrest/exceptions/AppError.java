package biz.itehnika.homeaccrest.exceptions;

import lombok.Data;

import java.util.Date;

@Data
public class AppError {
    private String message;
    private Date timestamp;

    public AppError(String message) {
        this.message = message;
        this.timestamp = new Date();
    }
}
