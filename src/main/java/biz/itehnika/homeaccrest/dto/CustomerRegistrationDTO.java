package biz.itehnika.homeaccrest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerRegistrationDTO {
    private String login;
    private String password;
    private String confirmPassword;
    private String email;
}
