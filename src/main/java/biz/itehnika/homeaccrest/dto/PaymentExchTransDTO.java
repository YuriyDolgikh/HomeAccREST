package biz.itehnika.homeaccrest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExchTransDTO {
    
    private String dateTime;
    private Long   srcAccountId;
    private Long   dstAccountId;
    private Double amount;
    
    
    public static PaymentExchTransDTO of(String dateTime,
                                         Long srcAccountId,
                                         Long dstAccountId,
                                         Double amount){
        return new PaymentExchTransDTO(dateTime, srcAccountId, dstAccountId, amount);
    }

}
