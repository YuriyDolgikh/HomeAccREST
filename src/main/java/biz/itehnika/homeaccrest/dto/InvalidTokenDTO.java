package biz.itehnika.homeaccrest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@AllArgsConstructor
@Data
public class InvalidTokenDTO {
    private String token;
    private LocalDateTime validDateTime;
}
