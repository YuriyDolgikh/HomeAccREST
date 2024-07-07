package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.Account;
import biz.itehnika.homeaccrest.models.PaymentCategory;
import biz.itehnika.homeaccrest.models.enums.AccountType;
import biz.itehnika.homeaccrest.models.enums.CurrencyName;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCategoryDTO {

    private String name;
    private String description;

    public static PaymentCategoryDTO of(String name, String description){
        return new PaymentCategoryDTO(name, description);
    }

    public static List<PaymentCategoryDTO> listOf(List<PaymentCategory> paymentCategories) {
        List<PaymentCategoryDTO> paymentCategoryDTOList = new ArrayList<>();
        for(PaymentCategory category : paymentCategories){
            paymentCategoryDTOList.add(of(category.getName(), category.getDescription()));
        }
        return paymentCategoryDTOList;
    }
}
