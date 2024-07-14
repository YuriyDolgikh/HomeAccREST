package biz.itehnika.homeaccrest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerPeriodDTO {
    String startDate;
    String endDate;
    
    public static CustomerPeriodDTO of(String startDate, String endDate){
        return new CustomerPeriodDTO(startDate, endDate);
    }
    
}
