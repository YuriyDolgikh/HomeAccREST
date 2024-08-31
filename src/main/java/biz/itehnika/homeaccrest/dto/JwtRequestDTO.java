package biz.itehnika.homeaccrest.dto;

import lombok.Data;

@Data
public class JwtRequestDTO {
    private String email;
    private String password;
}
