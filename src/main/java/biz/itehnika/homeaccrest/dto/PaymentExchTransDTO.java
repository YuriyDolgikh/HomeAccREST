package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentExchTransDTO {
    
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    
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
