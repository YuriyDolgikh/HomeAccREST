package biz.itehnika.homeaccrest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomerFiltersDTO {
    private Boolean isUAH;
    private Boolean isEUR;
    private Boolean isUSD;
    private Boolean isIN;
    private Boolean isOUT;
    private Boolean isCompleted;
    private Boolean isScheduled;
    
    public static CustomerFiltersDTO of(Boolean isUAH,
                                        Boolean isEUR,
                                        Boolean isUSD,
                                        Boolean isIN,
                                        Boolean isOUT,
                                        Boolean isCompleted,
                                        Boolean isScheduled
                                        ){
        return new CustomerFiltersDTO(isUAH, isEUR, isUSD, isIN, isOUT, isCompleted, isScheduled);
    }
    
}
