package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Payment;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    private Long id;
    private String dateTime;
    private Boolean direction;          // True - income, False - waste
    private Boolean status;             // True - Completed, False - Scheduled
    private Double amount;
    private CurrencyName currencyName;
    private String description;
    private String paymentCategoryName;
    private String accountName;



    public static PaymentDTO of(Long id,
                                String dateTime,
                                Boolean direction,
                                Boolean status,
                                Double amount,
                                CurrencyName currencyName,
                                String description,
                                String paymentCategory,
                                String account){
        return new PaymentDTO(id, dateTime, direction, status, amount, currencyName, description, paymentCategory, account);
    }
    
    public static PaymentDTO of(Payment payment) {
        return new PaymentDTO(payment.getId(),
                              payment.getDateTime().format(dateTimeFormatter),
                              payment.getDirection(),
                              payment.getStatus(),
                              payment.getAmount(),
                              payment.getCurrencyName(),
                              payment.getDescription(),
                              payment.getPaymentCategory().getName(),
                              payment.getAccount().getName()
        );
    }
    
    public static List<PaymentDTO> listOf(List<Payment> paymentsList) {
        List<PaymentDTO> paymentDTOList = new ArrayList<>();
        
        for (Payment payment : paymentsList){
            paymentDTOList.add(of(payment));
        }
        return paymentDTOList;
    }


}
