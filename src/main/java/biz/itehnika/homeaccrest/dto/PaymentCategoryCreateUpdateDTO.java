package biz.itehnika.homeaccrest.dto;

import biz.itehnika.homeaccrest.models.PaymentCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCategoryCreateUpdateDTO {

    private String name;
    private String description;

    public static PaymentCategoryCreateUpdateDTO of(String name, String description){
        return new PaymentCategoryCreateUpdateDTO(name, description);
    }

    public static List<PaymentCategoryCreateUpdateDTO> listOf(List<PaymentCategory> paymentCategories) {
        List<PaymentCategoryCreateUpdateDTO> paymentCategoryDTOList = new ArrayList<>();
        for(PaymentCategory category : paymentCategories){
            paymentCategoryDTOList.add(of(category.getName(), category.getDescription()));
        }
        return paymentCategoryDTOList;
    }
}
