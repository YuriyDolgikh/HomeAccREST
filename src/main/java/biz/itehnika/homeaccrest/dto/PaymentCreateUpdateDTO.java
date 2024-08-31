package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Payment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateUpdateDTO {
    
    static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    
    private String dateTime;
    private Boolean direction;          // True - income, False - waste
    private Boolean status;             // True - Completed, False - Scheduled
    private Double amount;
    private String description;
    private String paymentCategoryName;
    private String accountName;

    public static PaymentCreateUpdateDTO of(String dateTime,
                                            Boolean direction,
                                            Boolean status,
                                            Double amount,
                                            String description,
                                            String paymentCategory,
                                            String account){
        return new PaymentCreateUpdateDTO(dateTime, direction, status, amount, description, paymentCategory, account);
    }
    
    public static PaymentCreateUpdateDTO of(Payment payment) {
        return new PaymentCreateUpdateDTO(payment.getDateTime().format(dateTimeFormatter),
                                          payment.getDirection(),
                                          payment.getStatus(),
                                          payment.getAmount(),
                                          payment.getDescription(),
                                          payment.getPaymentCategory().getName(),
                                          payment.getAccount().getName()
        );
    }
    
//    public static List<PaymentCreateUpdateDTO> listOf(List<Payment> paymentsList) {
//        List<PaymentCreateUpdateDTO> paymentDTOList = new ArrayList<>();
//
//        for (Payment payment : paymentsList){
//            paymentDTOList.add(of(payment));
//        }
//        return paymentDTOList;
//    }

}
