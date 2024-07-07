package biz.itehnika.homeaccrest.dto;

import lombok.Data;

@Data
public class CustomerRegistrationDTO {
    private String login;
    private String password;
    private String confirmPassword;
    private String email;
}
